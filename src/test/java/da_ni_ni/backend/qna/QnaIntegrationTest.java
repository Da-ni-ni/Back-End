package da_ni_ni.backend.qna;

import da_ni_ni.backend.qna.domain.DailyQuestion;
import da_ni_ni.backend.qna.repository.DailyAnswerRepository;
import da_ni_ni.backend.qna.repository.DailyQuestionRepository;
import da_ni_ni.backend.user.domain.User;
import da_ni_ni.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
class QnaIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private DailyQuestionRepository questionRepo;
    @Autowired
    private DailyAnswerRepository answerRepo;
    @Autowired
    private UserRepository userRepo;

    private DailyQuestion todayQuestion;

    @BeforeEach
    void setUp() {
        // clean up
        answerRepo.deleteAll();
        questionRepo.deleteAll();
        userRepo.deleteAll();

        // create approved users in same group
        User user1 = new User();
        user1.setEmail("test");
        user1.setName("Test User");
        user1.setNickname("Tester");
        user1.setGroupId(1L);
        user1.setPasswordHash("dummy");            // ← 필수
        userRepo.save(user1);

        User user2 = new User();
        user2.setEmail("other");
        user2.setName("Other User");
        user2.setNickname("Other");
        user2.setGroupId(1L);
        user2.setPasswordHash("dummy");            // ← 필수
        userRepo.save(user2);

        // “논리적 오늘”을 오전 5시 기준으로 계산
        ZoneId zone = ZoneId.of("Asia/Seoul");
        LocalDate realToday = LocalDate.now(zone);
        LocalTime realNow = LocalTime.now(zone);
        LocalDate logicalToday = realNow.isBefore(LocalTime.of(5, 0))
                ? realToday.minusDays(1)
                : realToday;

        // insert question at 05:00 Seoul time and activate on logicalToday
        todayQuestion = new DailyQuestion();
        todayQuestion.setQuestion("What is your wish?");
        todayQuestion.setCreatedAt(realToday.atTime(5, 0));
        todayQuestion.setActivationDate(logicalToday);  // 논리적 오늘로 지정
        questionRepo.save(todayQuestion);
    }

    @Test
    @WithMockUser(username = "test")
    void everydayEndpoint_returnsTodayQuestion() throws Exception {
        mvc.perform(get("/api/v1/question/everyday")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.question").value("What is your wish?"));
    }

    @Test
    @WithMockUser(username = "test")
    void monthlyEndpoint_returnsList() throws Exception {
        // no extra questions: monthly list should include today's
        int year = todayQuestion.getCreatedAt().toLocalDate().getYear();
        int month = todayQuestion.getCreatedAt().toLocalDate().getMonthValue();

        mvc.perform(get("/api/v1/question/monthly")
                        .param("year", String.valueOf(year))
                        .param("month", String.valueOf(month))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].question_id").value(todayQuestion.getId()))
                .andExpect(jsonPath("[0].date").value(todayQuestion.getActivationDate().toString()));
    }

    @Test
    @WithMockUser(username = "test")
    void detailEndpoint_withoutAnswer_forbidden() throws Exception {
        mvc.perform(get("/api/v1/question/" + todayQuestion.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test")
    void fullFlow_registerUpdateDeleteAnswer() throws Exception {
        ZoneId zone = ZoneId.of("Asia/Seoul");

        // 1) 답변 등록
        mvc.perform(post("/api/v1/question/" + todayQuestion.getId() + "/answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answer\":\"My wish\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.createdAt").exists());

        // 2) 상세 조회
        mvc.perform(get("/api/v1/question/" + todayQuestion.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answers[?(@.nickname=='Tester')].answer")
                        .value("My wish"));

        // 3) 논리적 오늘 계산 후 수정
        LocalDate realDay = LocalDate.now(zone);
        LocalTime now = LocalTime.now(zone);
        LocalDate logicalToday = now.isBefore(LocalTime.of(5, 0))
                ? realDay.minusDays(1)
                : realDay;

        mvc.perform(put("/api/v1/question/" + todayQuestion.getId() + "/answers")
                        .param("date", logicalToday.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answer\":\"New wish\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("New wish"));

        // 4) 삭제: 오전 5시 이후로 강제 목킹
        mvc.perform(delete("/api/v1/question/" + todayQuestion.getId() + "/answers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.question_id")
                        .value(todayQuestion.getId()));
    }
}

