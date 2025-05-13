package da_ni_ni.backend.daily.repository;

import da_ni_ni.backend.daily.domain.Comment;
import da_ni_ni.backend.daily.domain.Daily;
import da_ni_ni.backend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByDailyId(Long dailyId);
    Optional<Comment> findCommentByCommentId(Long commentId);
    List<Comment> findByUser(User user);
    long countByDaily(Daily daily);
}
