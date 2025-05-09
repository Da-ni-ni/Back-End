package da_ni_ni.backend.qna.repository;

import da_ni_ni.backend.qna.domain.DailyAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DailyAnswerRepository extends JpaRepository<DailyAnswer, Long> {
    Optional<DailyAnswer> findByQuestionIdAndUserId(Long questionId, Long userId);
    void deleteByQuestionIdAndUserId(Long questionId, Long userId);
}