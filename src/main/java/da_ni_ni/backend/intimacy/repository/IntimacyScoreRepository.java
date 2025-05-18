package da_ni_ni.backend.intimacy.repository;

import da_ni_ni.backend.intimacy.domain.IntimacyScore;
import da_ni_ni.backend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IntimacyScoreRepository extends JpaRepository<IntimacyScore, Integer> {
    Optional<IntimacyScore> findFirstByUserOrderByTestDateDesc(User user);
}
