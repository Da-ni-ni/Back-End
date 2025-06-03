package da_ni_ni.backend.qna;

import da_ni_ni.backend.qna.domain.DailyQuestion;
import da_ni_ni.backend.qna.repository.DailyQuestionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class QuestionActivationSchedulerTest {

    @Mock
    private DailyQuestionRepository questionRepository;

    @Test
    public void testQuestionActivationAt5AM() {
        // 테스트용 날짜
        LocalDate testDate = LocalDate.of(2023, 5, 15);
        ZoneId zoneId = ZoneId.of("Asia/Seoul");

        // 실제 Clock을 사용해 명확한 동작 보장
        Clock fixedClock = Clock.fixed(
                testDate.atTime(5, 0).atZone(zoneId).toInstant(),
                zoneId
        );

        // 실제 Clock 사용하는 스케줄러 생성
        QuestionActivationScheduler realScheduler = new QuestionActivationScheduler(
                questionRepository,
                fixedClock
        );

        // 테스트 설정
        when(questionRepository.findByActivationDate(testDate)).thenReturn(Optional.empty());

        DailyQuestion nextQuestion = new DailyQuestion();
        nextQuestion.setId(1L);
        nextQuestion.setQuestion("테스트 질문");
        when(questionRepository.findFirstByActivationDateIsNullOrderByIdAsc()).thenReturn(Optional.of(nextQuestion));

        // 테스트 실행
        realScheduler.activateNextQuestion();

        // 검증
        verify(questionRepository).save(argThat(question ->
                question.getActivationDate() != null &&
                        question.getActivationDate().equals(testDate)
        ));

        // 추가 검증: 저장된 값 검증
        ArgumentCaptor<DailyQuestion> questionCaptor = ArgumentCaptor.forClass(DailyQuestion.class);
        verify(questionRepository).save(questionCaptor.capture());
        DailyQuestion savedQuestion = questionCaptor.getValue();

        assertEquals(testDate, savedQuestion.getActivationDate());
        assertEquals(1L, savedQuestion.getId());
        assertEquals("테스트 질문", savedQuestion.getQuestion());
    }

    @Test
    public void testNoActivationWhenQuestionAlreadyExists() {
        // 테스트용 날짜
        LocalDate testDate = LocalDate.of(2025, 6, 3);
        ZoneId zoneId = ZoneId.of("Asia/Seoul");

        // 실제 Clock을 사용해 명확한 동작 보장
        Clock fixedClock = Clock.fixed(
                testDate.atTime(5, 0).atZone(zoneId).toInstant(),
                zoneId
        );

        // 실제 Clock 사용하는 스케줄러 생성
        QuestionActivationScheduler realScheduler = new QuestionActivationScheduler(
                questionRepository,
                fixedClock
        );

        // 테스트 데이터 설정
        DailyQuestion alreadyActivated = new DailyQuestion();
        alreadyActivated.setId(1L);
        alreadyActivated.setQuestion("이미 활성화된 질문");
        alreadyActivated.setActivationDate(testDate);

        when(questionRepository.findByActivationDate(testDate)).thenReturn(Optional.of(alreadyActivated));

        // 테스트 실행
        realScheduler.activateNextQuestion();

        // 검증
        verify(questionRepository, never()).save(any(DailyQuestion.class));

        // 추가 검증: findFirstByActivationDateIsNullOrderByIdAsc가 호출되지 않았는지 확인
        verify(questionRepository, never()).findFirstByActivationDateIsNullOrderByIdAsc();
    }
}