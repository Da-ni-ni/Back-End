package da_ni_ni.backend.qna;

import da_ni_ni.backend.qna.repository.DailyQuestionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;

@Component
public class QuestionActivationScheduler {

    private final DailyQuestionRepository questionRepository;
    private final Clock clock;

    public QuestionActivationScheduler(DailyQuestionRepository questionRepository, Clock clock) {
        this.questionRepository = questionRepository;
        this.clock = clock;
    }

    @Scheduled(cron = "0 0 5 * * *", zone = "Asia/Seoul")
    @Transactional
    public void activateNextQuestion() {
        // Clock을 사용하여 현재 날짜 가져오기
        LocalDate today = LocalDate.now(clock);

        // 이미 오늘 활성화된 질문이 있으면 종료
        if (questionRepository.findByActivationDate(today).isPresent()) {
            return;
        }

        // 아직 activationDate가 없는 질문 중 ID 오름차순으로 하나 꺼내서 오늘 활성화
        questionRepository.findFirstByActivationDateIsNullOrderByIdAsc()
                .ifPresent(question -> {
                    question.setActivationDate(today);
                    questionRepository.save(question);
                });
    }
}