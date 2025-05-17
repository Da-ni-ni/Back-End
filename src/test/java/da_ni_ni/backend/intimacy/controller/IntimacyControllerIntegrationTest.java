package da_ni_ni.backend.intimacy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import da_ni_ni.backend.intimacy.dto.AnswerDto;
import da_ni_ni.backend.intimacy.dto.FamilyScoreResponse;
import da_ni_ni.backend.intimacy.dto.PersonalScoreResponse;
import da_ni_ni.backend.intimacy.dto.TestAnswerRequest;
import da_ni_ni.backend.intimacy.service.IntimacyService;
import da_ni_ni.backend.user.domain.User;
import da_ni_ni.backend.user.jwt.JwtTokenProvider;
import da_ni_ni.backend.user.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        properties = {
                // JwtTokenProvider를 MockBean으로 대체하므로 실제 Secret 값은 의미 없지만,
                // 어쨌든 ConfigurationProperties 바인딩을 위해 기본값을 넣어둘 수도 있습니다.
                "jwt.secret=dGVzdFNlY3JldEtleTEyMzQ1Njc3OA==",
                "jwt.expiration-ms=3600000"
        }
)
@AutoConfigureMockMvc(addFilters = false)   // ← 여기!
@ActiveProfiles("test")
public class IntimacyControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // IntimacyController 의존 빈들
    @MockBean
    private IntimacyService intimacyService;

    @MockBean
    private AuthService authService;

    // UserController/UserService → JwtTokenProvider 의존성을 무효화
    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private User testUser;
    private TestAnswerRequest testRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("테스트사용자")
                .build();
        when(authService.getCurrentUser()).thenReturn(testUser);

        List<AnswerDto> answers = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            answers.add(new AnswerDto(i, i % 5 + 1));
        }
        testRequest = new TestAnswerRequest();
        testRequest.setAnswers(answers);
    }

    @Test
    @DisplayName("테스트 답변 제출 API 성공")
    void submitAnswers_Success() throws Exception {
        doNothing().when(intimacyService).submitAnswers(eq(testUser), any());

        mockMvc.perform(post("/api/v1/tests/answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("테스트 답변 제출 검증 실패")
    void submitAnswers_ValidationFail_TooFewAnswers() throws Exception {
        TestAnswerRequest invalidReq = new TestAnswerRequest();
        invalidReq.setAnswers(List.of(new AnswerDto(1,3), new AnswerDto(2,4)));

        mockMvc.perform(post("/api/v1/tests/answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("개인 점수 조회 API 성공")
    void getPersonal_Success() throws Exception {
        PersonalScoreResponse resp = new PersonalScoreResponse("나", 75);
        when(intimacyService.getPersonalScore(testUser)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/tests/personal"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberName").value("나"))
                .andExpect(jsonPath("$.score").value(75));
    }

    @Test
    @DisplayName("가족 점수 조회 API 성공")
    void getFamily_Success() throws Exception {
        FamilyScoreResponse resp = new FamilyScoreResponse("테스트가족", 80.5);
        when(intimacyService.getFamilyScore(testUser)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/tests/family"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.familyName").value("테스트가족"))
                .andExpect(jsonPath("$.averageScore").value(80.5));
    }
}
