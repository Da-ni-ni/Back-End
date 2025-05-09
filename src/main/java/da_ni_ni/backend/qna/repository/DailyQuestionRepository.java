package da_ni_ni.backend.qna.repository;

import da_ni_ni.backend.qna.domain.DailyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyQuestionRepository extends JpaRepository<DailyQuestion, Long> {
    // 오늘 활성화된 질문
    Optional<DailyQuestion> findByActivationDate(LocalDate date);

    // 월간 활성화 질문
    List<DailyQuestion> findAllByActivationDateBetweenOrderByActivationDate(LocalDate start, LocalDate end);

    // activationDate가 아직 NULL인 질문 중 ID 순으로 하나만
    Optional<DailyQuestion> findFirstByActivationDateIsNullOrderByIdAsc();
}
