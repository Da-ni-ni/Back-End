package da_ni_ni.backend.refreshtoken;

import da_ni_ni.backend.user.domain.RefreshToken;
import da_ni_ni.backend.user.dto.TokenReissueRequestDto;
import da_ni_ni.backend.user.dto.TokenReissueResponseDto;
import da_ni_ni.backend.user.exception.ExpiredRefreshTokenException;
import da_ni_ni.backend.user.jwt.JwtTokenProvider;
import da_ni_ni.backend.user.repository.UserRepository;
import da_ni_ni.backend.user.service.RefreshTokenService;
import da_ni_ni.backend.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 리프레시 토큰 관련 테스트")
class UserServiceRefreshTokenTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private UserService userService;

    private final String testEmail = "test@example.com";
    private final String testRefreshToken = "test-refresh-token";
    private final String newAccessToken = "new-access-token";
    private final String newRefreshToken = "new-refresh-token";

    @Test
    @DisplayName("토큰 재발급 - 성공")
    void reissueToken_Success() {
        // given
        TokenReissueRequestDto request = new TokenReissueRequestDto();
        request.setRefreshToken(testRefreshToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .userEmail(testEmail)
                .token(testRefreshToken)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build();

        RefreshToken newRefreshTokenEntity = RefreshToken.builder()
                .userEmail(testEmail)
                .token(newRefreshToken)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build();

        when(refreshTokenService.findByTokenOrThrow(testRefreshToken)).thenReturn(refreshToken);
        when(refreshTokenService.verifyExpiration(refreshToken)).thenReturn(refreshToken);
        when(jwtTokenProvider.createAccessToken(testEmail)).thenReturn(newAccessToken);
        when(refreshTokenService.createRefreshToken(testEmail)).thenReturn(newRefreshTokenEntity);

        // when
        TokenReissueResponseDto result = userService.reissueToken(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo(newAccessToken);
        assertThat(result.getRefreshToken()).isEqualTo(newRefreshToken);

        verify(refreshTokenService).findByTokenOrThrow(testRefreshToken);
        verify(refreshTokenService).verifyExpiration(refreshToken);
        verify(jwtTokenProvider).createAccessToken(testEmail);
        verify(refreshTokenService).createRefreshToken(testEmail);
    }

    @Test
    @DisplayName("토큰 재발급 - 유효하지 않은 리프레시 토큰")
    void reissueToken_InvalidRefreshToken() {
        // given
        TokenReissueRequestDto request = new TokenReissueRequestDto();
        request.setRefreshToken(testRefreshToken);

        when(refreshTokenService.findByTokenOrThrow(testRefreshToken)).thenThrow(new RuntimeException("유효하지 않은 refresh token입니다."));

        // when & then
        assertThatThrownBy(() -> userService.reissueToken(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("유효하지 않은 refresh token입니다.");

        verify(refreshTokenService).findByTokenOrThrow(testRefreshToken);
        verify(refreshTokenService, never()).verifyExpiration(any());
        verify(jwtTokenProvider, never()).createAccessToken(any());
        verify(refreshTokenService, never()).createRefreshToken(any());
    }

    @Test
    @DisplayName("토큰 재발급 - 만료된 리프레시 토큰")
    void reissueToken_ExpiredRefreshToken() {
        // given
        TokenReissueRequestDto request = new TokenReissueRequestDto();
        request.setRefreshToken(testRefreshToken);

        RefreshToken expiredRefreshToken = RefreshToken.builder()
                .userEmail(testEmail)
                .token(testRefreshToken)
                .expiryDate(LocalDateTime.now().minusDays(1)) // 만료된 토큰
                .createdAt(LocalDateTime.now().minusDays(8))
                .build();

        when(refreshTokenService.findByTokenOrThrow(testRefreshToken)).thenReturn(expiredRefreshToken);
        when(refreshTokenService.verifyExpiration(expiredRefreshToken))
                .thenThrow(new ExpiredRefreshTokenException("Refresh token이 만료되었습니다. 다시 로그인해주세요."));

        // when & then
        assertThatThrownBy(() -> userService.reissueToken(request))
                .isInstanceOf(ExpiredRefreshTokenException.class)
                .hasMessage("Refresh token이 만료되었습니다. 다시 로그인해주세요.");

        verify(refreshTokenService).findByTokenOrThrow(testRefreshToken);
        verify(refreshTokenService).verifyExpiration(expiredRefreshToken);
        verify(jwtTokenProvider, never()).createAccessToken(any());
        verify(refreshTokenService, never()).createRefreshToken(any());
    }

    @Test
    @DisplayName("로그아웃 - 리프레시 토큰 삭제")
    void logout_Success() {
        // when
        userService.logout(testEmail);

        // then
        verify(refreshTokenService).deleteByUserEmail(testEmail);
    }
}