package da_ni_ni.backend.user.repository;

import da_ni_ni.backend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);

    // 그룹 ID로 해당 그룹에 속한 모든 사용자 조회
    List<User> findAllByGroupId(Long groupId);
}