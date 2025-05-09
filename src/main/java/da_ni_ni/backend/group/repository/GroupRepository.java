package da_ni_ni.backend.group.repository;

import da_ni_ni.backend.group.domain.Group;
import da_ni_ni.backend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group>findByInviteCode(String inviteCode);
    Optional<Group>findByAdminUser(User user);
}
