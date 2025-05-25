package da_ni_ni.backend.refreshtoken;

import da_ni_ni.backend.user.domain.RefreshToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RefreshToken 엔티티 테스트")
class RefreshTokenEntityTest {

    private RefreshToken refreshToken;
    private String testToken;
    private String testUserEmail;
    private LocalDateTime testExpiryDate;
    private LocalDateTime testCreatedAt;

    @BeforeEach
    void setUp() {
        testToken = "test-refresh-token-uuid";
        testUserEmail = "test@example.com";
        testExpiryDate = LocalDateTime.now().plusDays(7);
        testCreatedAt = LocalDateTime.now();

        refreshToken = RefreshToken.builder()
                .token(testToken)
                .userEmail(testUserEmail)
                .expiryDate(testExpiryDate)
                .createdAt(testCreatedAt)
                .build();
    }

    @Test
    @DisplayName("RefreshToken 빌더 패턴으로 생성 테스트")
    void createRefreshTokenWithBuilder() {
        // then
        assertThat(refreshToken.getToken()).isEqualTo(testToken);
        assertThat(refreshToken.getUserEmail()).isEqualTo(testUserEmail);
        assertThat(refreshToken.getExpiryDate()).isEqualTo(testExpiryDate);
        assertThat(refreshToken.getCreatedAt()).isEqualTo(testCreatedAt);
    }

    @Test
    @DisplayName("RefreshToken 만료 확인 - 만료되지 않은 경우")
    void isNotExpired() {
        // given
        LocalDateTime futureDate = LocalDateTime.now().plusDays(1);
        refreshToken.setExpiryDate(futureDate);

        // when & then
        assertThat(refreshToken.isExpired()).isFalse();
    }

    @Test
    @DisplayName("RefreshToken 만료 확인 - 만료된 경우")
    void isExpired() {
        // given
        LocalDateTime pastDate = LocalDateTime.now().minusDays(1);
        refreshToken.setExpiryDate(pastDate);

        // when & then
        assertThat(refreshToken.isExpired()).isTrue();
    }

    @Test
    @DisplayName("RefreshToken 만료 확인 - 현재 시간과 동일한 경우")
    void isExpiredAtCurrentTime() {
        // given
        LocalDateTime now = LocalDateTime.now();
        refreshToken.setExpiryDate(now);

        // when & then
        // 현재 시간보다 이후인지 확인하므로, 동일한 시간은 만료된 것으로 간주
        assertThat(refreshToken.isExpired()).isTrue();
    }

    @Test
    @DisplayName("RefreshToken Getter/Setter 테스트")
    void getterSetterTest() {
        // given
        Long newId = 1L;
        String newToken = "new-token";
        String newUserEmail = "new@example.com";
        LocalDateTime newExpiryDate = LocalDateTime.now().plusDays(14);
        LocalDateTime newCreatedAt = LocalDateTime.now().minusHours(1);

        // when
        refreshToken.setId(newId);
        refreshToken.setToken(newToken);
        refreshToken.setUserEmail(newUserEmail);
        refreshToken.setExpiryDate(newExpiryDate);
        refreshToken.setCreatedAt(newCreatedAt);

        // then
        assertThat(refreshToken.getId()).isEqualTo(newId);
        assertThat(refreshToken.getToken()).isEqualTo(newToken);
        assertThat(refreshToken.getUserEmail()).isEqualTo(newUserEmail);
        assertThat(refreshToken.getExpiryDate()).isEqualTo(newExpiryDate);
        assertThat(refreshToken.getCreatedAt()).isEqualTo(newCreatedAt);
    }

    @Test
    @DisplayName("RefreshToken 필드 null 값 테스트")
    void nullValueTest() {
        // given
        RefreshToken emptyToken = RefreshToken.builder().build();

        // when & then
        assertThat(emptyToken.getId()).isNull();
        assertThat(emptyToken.getToken()).isNull();
        assertThat(emptyToken.getUserEmail()).isNull();
        assertThat(emptyToken.getExpiryDate()).isNull();
        assertThat(emptyToken.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("RefreshToken AllArgsConstructor 테스트")
    void allArgsConstructorTest() {
        // given
        Long id = 1L;
        String token = "test-token";
        String userEmail = "test@example.com";
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(7);
        LocalDateTime createdAt = LocalDateTime.now();

        // when
        RefreshToken token1 = RefreshToken.builder()
                .id(id)
                .token(token)
                .userEmail(userEmail)
                .expiryDate(expiryDate)
                .createdAt(createdAt)
                .build();

        // then
        assertThat(token1.getId()).isEqualTo(id);
        assertThat(token1.getToken()).isEqualTo(token);
        assertThat(token1.getUserEmail()).isEqualTo(userEmail);
        assertThat(token1.getExpiryDate()).isEqualTo(expiryDate);
        assertThat(token1.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("RefreshToken 시간 경계값 테스트")
    void timeBoundaryTest() {
        // given
        LocalDateTime exactNow = LocalDateTime.now();
        LocalDateTime oneMicroSecondBefore = exactNow.minusNanos(1000);
        LocalDateTime oneMicroSecondAfter = exactNow.plusNanos(1000);

        // when & then - 1 마이크로초 전은 만료
        refreshToken.setExpiryDate(oneMicroSecondBefore);
        assertThat(refreshToken.isExpired()).isTrue();

        // when & then - 1 마이크로초 후는 만료되지 않음
        refreshToken.setExpiryDate(oneMicroSecondAfter);
        assertThat(refreshToken.isExpired()).isFalse();
    }
}