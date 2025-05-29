package da_ni_ni.backend.daily.repository;

import da_ni_ni.backend.daily.domain.Daily;
import da_ni_ni.backend.daily.domain.DailyLike;
import da_ni_ni.backend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<DailyLike, Long> {
    List<DailyLike> findAllByDaily(Daily daily);
    boolean existsByDailyAndUser(Daily daily, User user);
    Optional<DailyLike> findByDailyAndUser(Daily daily, User user);
    // daily와 연관된 DailyLike 개수 세기
    long countByDaily(Daily daily);
}