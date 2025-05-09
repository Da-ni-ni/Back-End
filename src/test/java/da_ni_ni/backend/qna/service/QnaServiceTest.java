package da_ni_ni.backend.qna.service;

import da_ni_ni.backend.qna.domain.DailyQuestion;
import da_ni_ni.backend.qna.dto.DailyQuestionDto;
import da_ni_ni.backend.qna.dto.MonthlyQuestionDto;
import da_ni_ni.backend.qna.exception.BadRequestException;
import da_ni_ni.backend.qna.exception.ForbiddenException;
import da_ni_ni.backend.qna.repository.DailyAnswerRepository;
import da_ni_ni.backend.qna.repository.DailyQuestionRepository;
import da_ni_ni.backend.user.domain.User;
import da_ni_ni.backend.user.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class QnaServiceTest {

    @Mock private DailyQuestionRepository questionRepo;
    @Mock private DailyAnswerRepository answerRepo;
    @Mock private AuthService authService;
    @InjectMocks private QnaService qnaService;

    private User approvedUser;

    @BeforeEach
    void setup() {
        approvedUser = new User();
        approvedUser.setUserId(42L);
        approvedUser.setGroupId(100L);
        given(authService.getApprovedUser()).willReturn(approvedUser);
        given(authService.getFamilyMembers()).willReturn(List.of(approvedUser));
    }

    @Test
    void getTodayQuestion_success() {
        LocalDateTime now = LocalDate.now(ZoneId.of("Asia/Seoul")).atTime(6, 0);
        DailyQuestion q = new DailyQuestion();
        q.setId(1L);
        q.setQuestion("Q?");
        q.setCreatedAt(now);
        given(questionRepo.findAllByActivationDateBetweenOrderByActivationDate(any(), any())).
                willReturn(List.of(q));

        DailyQuestionDto dto = qnaService.getTodayQuestion();
        assertEquals(1L, dto.dailyId());
        assertEquals("Q?", dto.dailyQuestion());
    }

    @Test
    void getTodayQuestion_notPrepared_throws() {
        given(questionRepo.findAllByActivationDateBetweenOrderByActivationDate(any(), any()))
                .willReturn(Collections.emptyList());
        assertThrows(BadRequestException.class,
                () -> qnaService.getTodayQuestion());
    }

    @Test
    void getMonthlyQuestions_success() {
        DailyQuestion q1 = new DailyQuestion();
        q1.setId(10L);
        q1.setQuestion("M1");
        q1.setCreatedAt(LocalDate.of(2025,4,10).atStartOfDay());
        given(questionRepo.findAllByActivationDateBetweenOrderByActivationDate(any(), any()))
                .willReturn(List.of(q1));

        List<MonthlyQuestionDto> list = qnaService.getMonthlyQuestions(2025, 4);
        assertEquals(1, list.size());
        assertEquals(10L, list.get(0).dailyId());
    }

    @Test
    void getQuestionDetail_withoutAnswer_throws() {
        long qid = 5L;
        given(answerRepo.findByQuestionIdAndUserId(qid, approvedUser.getUserId()))
                .willReturn(Optional.empty());
        assertThrows(ForbiddenException.class,
                () -> qnaService.getQuestionDetail(qid));
    }
}