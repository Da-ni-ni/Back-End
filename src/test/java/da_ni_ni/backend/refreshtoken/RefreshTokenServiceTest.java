package da_ni_ni.backend.refreshtoken;

import da_ni_ni.backend.user.domain.RefreshToken;
import da_ni_ni.backend.user.exception.ExpiredRefreshTokenException;
import da_ni_ni.backend.user.exception.InvalidRefreshTokenException;
import da_ni_ni.backend.user.repository.RefreshTokenRepository;
import da_ni_ni.backend.user.service.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenService 테스트")
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private final String testEmail = "test@example.com";
    private final String testToken = "test-refresh-token";
    private final long refreshTokenDurationMs = 604800000L; // 7일

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDurationMs", refreshTokenDurationMs);
    }

    @Test
    @DisplayName("리프레시 토큰 생성 - 성공")
    void createRefreshToken_Success() {
        // given
        RefreshToken savedToken = RefreshToken.builder()
                .id(1L)
                .userEmail(testEmail)
                .token(testToken)
                .expiryDate(LocalDateTime.now().plusSeconds(refreshTokenDurationMs / 1000))
                .createdAt(LocalDateTime.now())
                .build();

        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(savedToken);

        // when
        RefreshToken result = refreshTokenService.createRefreshToken(testEmail);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUserEmail()).isEqualTo(testEmail);
        assertThat(result.getToken()).isNotNull();
        assertThat(result.getExpiryDate()).isAfter(LocalDateTime.now());

        // 기존 토큰 삭제 확인
        verify(refreshTokenRepository).deleteByUserEmail(testEmail);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("리프레시 토큰 조회 - 성공")
    void findByTokenOrThrow_Success() {
        // given
        RefreshToken token = RefreshToken.builder()
                .userEmail(testEmail)
                .token(testToken)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build();

        when(refreshTokenRepository.findByToken(testToken)).thenReturn(Optional.of(token));

        // when
        RefreshToken result = refreshTokenService.findByTokenOrThrow(testToken);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo(testToken);
        assertThat(result.getUserEmail()).isEqualTo(testEmail);
    }

    @Test
    @DisplayName("리프레시 토큰 조회 - 토큰이 존재하지 않음")
    void findByTokenOrThrow_TokenNotFound() {
        // given
        when(refreshTokenRepository.findByToken(testToken)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> refreshTokenService.findByTokenOrThrow(testToken))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessage("유효하지 않은 refresh token입니다.");
    }

    @Test
    @DisplayName("토큰 만료 검증 - 유효한 토큰")
    void verifyExpiration_ValidToken() {
        // given
        RefreshToken token = RefreshToken.builder()
                .userEmail(testEmail)
                .token(testToken)
                .expiryDate(LocalDateTime.now().plusDays(7)) // 미래 시간
                .createdAt(LocalDateTime.now())
                .build();

        // when
        RefreshToken result = refreshTokenService.verifyExpiration(token);

        // then
        assertThat(result).isEqualTo(token);
        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    @DisplayName("토큰 만료 검증 - 만료된 토큰")
    void verifyExpiration_ExpiredToken() {
        // given
        RefreshToken expiredToken = RefreshToken.builder()
                .userEmail(testEmail)
                .token(testToken)
                .expiryDate(LocalDateTime.now().minusDays(1)) // 과거 시간
                .createdAt(LocalDateTime.now().minusDays(8))
                .build();

        // when & then
        assertThatThrownBy(() -> refreshTokenService.verifyExpiration(expiredToken))
                .isInstanceOf(ExpiredRefreshTokenException.class)
                .hasMessage("Refresh token이 만료되었습니다. 다시 로그인해주세요.");

        verify(refreshTokenRepository).delete(expiredToken);
    }

    @Test
    @DisplayName("사용자 이메일로 토큰 삭제")
    void deleteByUserEmail() {
        // when
        refreshTokenService.deleteByUserEmail(testEmail);

        // then
        verify(refreshTokenRepository).deleteByUserEmail(testEmail);
    }

    @Test
    @DisplayName("토큰으로 삭제")
    void deleteByToken() {
        // when
        refreshTokenService.deleteByToken(testToken);

        // then
        verify(refreshTokenRepository).deleteByToken(testToken);
    }
}