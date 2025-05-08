package da_ni_ni.backend.qna.scheduler;

import da_ni_ni.backend.qna.repository.DailyQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class QuestionActivationScheduler {
    private final DailyQuestionRepository questionRepo;

    /**
     * 매일 오전 5시(서울 시간)에 호출.
     * 1) 오늘 날짜로 이미 활성화된 질문이 있으면 아무 작업도 하지 않고,
     * 2) 없으면 activation_date IS NULL 상태인 질문 중 가장 작은 ID 하나를 오늘 날짜로 활성화.
     */
    @Scheduled(cron = "0 0 5 * * *", zone = "Asia/Seoul")
    @Transactional
    public void activateNextQuestion() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        // 이미 오늘 활성화된 질문이 있으면 종료
        if (questionRepo.findByActivationDate(today).isPresent()) {
            return;
        }
        // 아직 activationDate가 없는 질문 중 ID 오름차순으로 하나 꺼내서 오늘 활성화
        questionRepo.findFirstByActivationDateIsNullOrderByIdAsc()
                .ifPresent(q -> {
                    q.setActivationDate(today);
                    questionRepo.save(q);
                });
    }
}
