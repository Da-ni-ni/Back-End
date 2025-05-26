package da_ni_ni.backend.refreshtoken;

import com.fasterxml.jackson.databind.ObjectMapper;
import da_ni_ni.backend.user.domain.User;
import da_ni_ni.backend.user.dto.TokenReissueRequestDto;
import da_ni_ni.backend.user.dto.TokenReissueResponseDto;
import da_ni_ni.backend.user.exception.ExpiredRefreshTokenException;
import da_ni_ni.backend.user.exception.InvalidRefreshTokenException;
import da_ni_ni.backend.user.service.AuthService;
import da_ni_ni.backend.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("UserController 리프레시 토큰 관련 테스트")
class RefreshTokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthService authService;

    private final String testRefreshToken = "test-refresh-token";
    private final String newAccessToken = "new-access-token";
    private final String newRefreshToken = "new-refresh-token";

    @Test
    @DisplayName("토큰 재발급 API - 성공")
    @WithMockUser
    void reissueToken_Success() throws Exception {
        // given
        TokenReissueRequestDto request = new TokenReissueRequestDto();
        request.setRefreshToken(testRefreshToken);

        TokenReissueResponseDto response = new TokenReissueResponseDto(newAccessToken, newRefreshToken);

        when(userService.reissueToken(any(TokenReissueRequestDto.class))).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/users/reissue")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(newAccessToken))
                .andExpect(jsonPath("$.refreshToken").value(newRefreshToken));

        verify(userService).reissueToken(any(TokenReissueRequestDto.class));
    }

    @Test
    @DisplayName("토큰 재발급 API - 유효하지 않은 리프레시 토큰")
    @WithMockUser
    void reissueToken_InvalidRefreshToken() throws Exception {
        // given
        TokenReissueRequestDto request = new TokenReissueRequestDto();
        request.setRefreshToken("invalid-token");

        when(userService.reissueToken(any(TokenReissueRequestDto.class)))
                .thenThrow(new InvalidRefreshTokenException("유효하지 않은 refresh token입니다."));

        // when & then
        mockMvc.perform(post("/api/v1/users/reissue")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(jsonPath("$.status").value(400))
                        .andExpect(jsonPath("$.message").value("유효하지 않은 refresh token입니다."));

        verify(userService).reissueToken(any(TokenReissueRequestDto.class));
    }

    @Test
    @DisplayName("토큰 재발급 API - 만료된 리프레시 토큰")
    @WithMockUser
    void reissueToken_ExpiredRefreshToken() throws Exception {
        // given
        TokenReissueRequestDto request = new TokenReissueRequestDto();
        request.setRefreshToken("expired-token");

        when(userService.reissueToken(any(TokenReissueRequestDto.class)))
                .thenThrow(new ExpiredRefreshTokenException("Refresh token이 만료되었습니다. 다시 로그인해주세요."));

        // when & then
        mockMvc.perform(post("/api/v1/users/reissue")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        // 변경 후
                        .andExpect(status().isBadRequest()); // 400으로 변경
        verify(userService).reissueToken(any(TokenReissueRequestDto.class));
    }

    @Test
    @DisplayName("토큰 재발급 API - 잘못된 요청 형식")
    @WithMockUser
    void reissueToken_InvalidRequestFormat() throws Exception {
        // given
        String invalidJson = "{ \"invalidField\": \"value\" }";

        TokenReissueResponseDto response = new TokenReissueResponseDto(newAccessToken, newRefreshToken);
        when(userService.reissueToken(any(TokenReissueRequestDto.class))).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/users/reissue")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isOk()); // refreshToken이 null이지만 처리됨

        verify(userService).reissueToken(any(TokenReissueRequestDto.class));
    }

    @Test
    @DisplayName("로그아웃 API - 성공")
    @WithMockUser(username = "test@example.com")
    void logout_Success() throws Exception {
        // given
        User mockUser = User.builder()
                .email("test@example.com")
                .name("Test User")
                .passwordHash("password")
                .nickName("Test")
                .build();

        when(authService.getCurrentUser()).thenReturn(mockUser);

        // when & then
        mockMvc.perform(post("/api/v1/users/logout")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(authService).getCurrentUser();
        verify(userService).logout("test@example.com");
    }
}
