package da_ni_ni.backend.refreshtoken;

import com.fasterxml.jackson.databind.ObjectMapper;
import da_ni_ni.backend.user.domain.RefreshToken;
import da_ni_ni.backend.user.domain.User;
import da_ni_ni.backend.user.dto.LoginRequestDto;
import da_ni_ni.backend.user.dto.LoginResponseDto;
import da_ni_ni.backend.user.dto.TokenReissueRequestDto;
import da_ni_ni.backend.user.repository.RefreshTokenRepository;
import da_ni_ni.backend.user.repository.UserRepository;
import da_ni_ni.backend.user.service.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestJwtConfiguration.class)

@ActiveProfiles("test")
@Transactional
@TestPropertySource(locations = "classpath:application-test.yml") // 명시적으로 프로퍼티 소스 지정

public class RefreshTokenIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = User.builder()
                .name("테스트유저")
                .email("test@example.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .nickName("테스트유저")
                .build();
        userRepository.save(testUser);
    }

    @Test
    void 로그인_시_리프레시토큰_발급() throws Exception {
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()));
    }

    @Test
    void 리프레시토큰으로_새로운_액세스토큰_발급() throws Exception {
        // 먼저 로그인하여 리프레시 토큰 획득
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        String loginResponse = mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        LoginResponseDto loginDto = objectMapper.readValue(loginResponse, LoginResponseDto.class);
        String refreshToken = loginDto.getRefreshToken();

        // 리프레시 토큰으로 새로운 액세스 토큰 요청
        TokenReissueRequestDto reissueRequest = new TokenReissueRequestDto();
        reissueRequest.setRefreshToken(refreshToken);

        mockMvc.perform(post("/api/v1/users/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reissueRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()));
    }

    @Test
    void 만료된_리프레시토큰으로_토큰재발급_실패() throws Exception {
        // 디버그 코드 추가
        try {
            // 테스트 코드 실행
            mockMvc.perform(post("/api/v1/users/reissue")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"refreshToken\":\"expired-refresh-token\"}"))
                    .andExpect(status().isBadRequest());
        } catch (Exception e) {
            // 실제 발생하는 예외 로깅
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    void 존재하지_않는_리프레시토큰으로_토큰재발급_실패() throws Exception {
        TokenReissueRequestDto reissueRequest = new TokenReissueRequestDto();
        reissueRequest.setRefreshToken("non-existent-refresh-token");

        mockMvc.perform(post("/api/v1/users/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reissueRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void 로그아웃_시_리프레시토큰_삭제() throws Exception {
        // 먼저 로그인하여 리프레시 토큰 생성
        RefreshToken refreshToken = refreshTokenService.createRefreshToken("test@example.com");

        // 로그아웃 전에 리프레시 토큰이 존재하는지 확인
        assert refreshTokenRepository.findByUserEmail("test@example.com").isPresent();

        // 로그인하여 액세스 토큰 획득 후 로그아웃
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        String loginResponse = mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        LoginResponseDto loginDto = objectMapper.readValue(loginResponse, LoginResponseDto.class);
        String accessToken = loginDto.getToken();

        // 로그아웃
        mockMvc.perform(post("/api/v1/users/logout")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        // 리프레시 토큰이 삭제되었는지 확인
        assert refreshTokenRepository.findByUserEmail("test@example.com").isEmpty();
    }
}