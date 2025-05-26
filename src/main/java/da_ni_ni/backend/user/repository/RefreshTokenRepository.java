package da_ni_ni.backend.user.repository;

import da_ni_ni.backend.user.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUserEmail(String userEmail);
    void deleteByUserEmail(String userEmail);
    void deleteByToken(String token);
}