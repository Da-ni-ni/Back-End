package da_ni_ni.backend.group.repository;

import da_ni_ni.backend.group.domain.FamilyGroup;
import da_ni_ni.backend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupRepository extends JpaRepository<FamilyGroup, Long> {
    boolean existsByAdminUser(User user);
    Optional<FamilyGroup>findById(Long id);
    Optional<FamilyGroup>findByInviteCode(String inviteCode);
    Optional<FamilyGroup>findByAdminUser(User user);
}
