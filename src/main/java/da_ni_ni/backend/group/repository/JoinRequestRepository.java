package da_ni_ni.backend.group.repository;

import da_ni_ni.backend.group.domain.Group;
import da_ni_ni.backend.group.domain.JoinReq;
import da_ni_ni.backend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JoinRequestRepository extends JpaRepository<JoinReq, Long> {
    Optional<JoinReq> findByUserAndGroup(User user, Group group);
    Optional<JoinReq> findByUser(User user);
    List<JoinReq> findByGroupAndStatus(Group group, JoinReq.RequestStatus status);
    List<JoinReq> findAllByInviteCode(String inviteCode);
}
