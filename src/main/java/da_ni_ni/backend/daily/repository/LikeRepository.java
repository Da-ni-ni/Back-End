package da_ni_ni.backend.daily.repository;

import da_ni_ni.backend.daily.domain.Daily;
import da_ni_ni.backend.daily.domain.Like;
import da_ni_ni.backend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    List<Like> findAllByDaily(Daily daily);
    Optional<Like> findByDailyAndUser(Daily daily, User user);
    // daily와 연관된 Like 개수 세기
    long countByDaily(Daily daily);
}