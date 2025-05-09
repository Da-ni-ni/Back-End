package da_ni_ni.backend.qna.controller;

import da_ni_ni.backend.qna.dto.DailyQuestionDto;
import da_ni_ni.backend.qna.dto.MonthlyQuestionDto;
import da_ni_ni.backend.qna.service.QnaService;
import da_ni_ni.backend.user.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QnaController.class)
class QnaControllerTest {

    @Autowired
    private MockMvc mvc;
    @MockBean
    private QnaService qnaService;
    @MockBean private AuthService authService;

    @Test
    @WithMockUser(username = "test@example.com")  // <- 추가
    void getTodayQuestion_endpoint() throws Exception {
        given(qnaService.getTodayQuestion())
                .willReturn(new DailyQuestionDto(99L, "Today's?"));

        mvc.perform(get("/api/v1/question/everyday"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.question").value("Today's?"));
    }

    @Test
    @WithMockUser(username = "test@example.com")  // ← 추가!
    void getMonthlyQuestions_endpoint() throws Exception {
        given(qnaService.getMonthlyQuestions(2025,4))
                .willReturn(List.of(
                        new MonthlyQuestionDto(1L, "2025-04-10", "Q1")
                ));

        mvc.perform(get("/api/v1/question/monthly")
                        .param("year","2025").param("month","4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].question_id").value(1));
    }
}
