package da_ni_ni.backend.refreshtoken;

import da_ni_ni.backend.user.domain.RefreshToken;
import da_ni_ni.backend.user.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("RefreshTokenRepository 테스트")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)

class RefreshTokenRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private final String testEmail = "test@example.com";
    private final String testToken = "test-refresh-token-12345";
    private RefreshToken savedToken;

    @BeforeEach
    void setUp() {
        savedToken = RefreshToken.builder()
                .userEmail(testEmail)
                .token(testToken)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build();

        entityManager.persistAndFlush(savedToken);
    }

    @Test
    @DisplayName("토큰으로 리프레시 토큰 조회 - 성공")
    void findByToken_Success() {
        // when
        Optional<RefreshToken> result = refreshTokenRepository.findByToken(testToken);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo(testToken);
        assertThat(result.get().getUserEmail()).isEqualTo(testEmail);
    }

    @Test
    @DisplayName("토큰으로 리프레시 토큰 조회 - 존재하지 않는 토큰")
    void findByToken_NotFound() {
        // when
        Optional<RefreshToken> result = refreshTokenRepository.findByToken("non-existent-token");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("사용자 이메일로 리프레시 토큰 조회 - 성공")
    void findByUserEmail_Success() {
        // when
        Optional<RefreshToken> result = refreshTokenRepository.findByUserEmail(testEmail);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getUserEmail()).isEqualTo(testEmail);
        assertThat(result.get().getToken()).isEqualTo(testToken);
    }

    @Test
    @DisplayName("사용자 이메일로 리프레시 토큰 조회 - 존재하지 않는 이메일")
    void findByUserEmail_NotFound() {
        // when
        Optional<RefreshToken> result = refreshTokenRepository.findByUserEmail("nonexistent@example.com");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("사용자 이메일로 리프레시 토큰 삭제")
    void deleteByUserEmail() {
        // given
        assertThat(refreshTokenRepository.findByUserEmail(testEmail)).isPresent();

        // when
        refreshTokenRepository.deleteByUserEmail(testEmail);
        entityManager.flush();

        // then
        assertThat(refreshTokenRepository.findByUserEmail(testEmail)).isEmpty();
    }

    @Test
    @DisplayName("토큰으로 리프레시 토큰 삭제")
    void deleteByToken() {
        // given
        assertThat(refreshTokenRepository.findByToken(testToken)).isPresent();

        // when
        refreshTokenRepository.deleteByToken(testToken);
        entityManager.flush();

        // then
        assertThat(refreshTokenRepository.findByToken(testToken)).isEmpty();
    }

    @Test
    @DisplayName("동일한 사용자의 기존 토큰 교체")
    void replaceExistingToken() {
        // given
        String newToken = "new-refresh-token-67890";
        RefreshToken newRefreshToken = RefreshToken.builder()
                .userEmail(testEmail)
                .token(newToken)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build();

        // when
        refreshTokenRepository.deleteByUserEmail(testEmail);
        entityManager.flush();
        entityManager.persistAndFlush(newRefreshToken);

        // then
        Optional<RefreshToken> oldToken = refreshTokenRepository.findByToken(testToken);
        Optional<RefreshToken> newTokenResult = refreshTokenRepository.findByToken(newToken);

        assertThat(oldToken).isEmpty();
        assertThat(newTokenResult).isPresent();
        assertThat(newTokenResult.get().getUserEmail()).isEqualTo(testEmail);
        assertThat(newTokenResult.get().getToken()).isEqualTo(newToken);
    }

    @Test
    @DisplayName("여러 사용자의 토큰이 있을 때 특정 사용자만 삭제")
    void deleteByUserEmail_MultipleUsers() {
        // given
        String anotherEmail = "another@example.com";
        String anotherToken = "another-refresh-token";

        RefreshToken anotherUserToken = RefreshToken.builder()
                .userEmail(anotherEmail)
                .token(anotherToken)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build();

        entityManager.persistAndFlush(anotherUserToken);

        // when
        refreshTokenRepository.deleteByUserEmail(testEmail);
        entityManager.flush();

        // then
        assertThat(refreshTokenRepository.findByUserEmail(testEmail)).isEmpty();
        assertThat(refreshTokenRepository.findByUserEmail(anotherEmail)).isPresent();
        assertThat(refreshTokenRepository.findByToken(anotherToken)).isPresent();
    }
}