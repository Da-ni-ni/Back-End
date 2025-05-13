package da_ni_ni.backend.emotion.repository;

import da_ni_ni.backend.emotion.domain.Emotion;
import da_ni_ni.backend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EmotionRepository extends JpaRepository<Emotion, Long> {
    Optional<Emotion>findById(Long emotionId);
    Optional<Emotion>findByUser(User user);
    List<Emotion>findAllByUser_FamilyGroupId(Long groupId);
}
