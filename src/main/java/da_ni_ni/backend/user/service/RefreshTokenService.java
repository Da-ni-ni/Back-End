package da_ni_ni.backend.user.service;

import da_ni_ni.backend.user.domain.RefreshToken;
import da_ni_ni.backend.user.exception.ExpiredRefreshTokenException;
import da_ni_ni.backend.user.exception.InvalidRefreshTokenException;
import da_ni_ni.backend.user.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration-ms:604800000}") // 7일 (기본값)
    private long refreshTokenDurationMs;

    public RefreshToken createRefreshToken(String userEmail) {
        // 기존 리프레시 토큰이 있다면 삭제
        refreshTokenRepository.deleteByUserEmail(userEmail);

        RefreshToken refreshToken = RefreshToken.builder()
                .userEmail(userEmail)
                .token(UUID.randomUUID().toString())
                .expiryDate(LocalDateTime.now().plusSeconds(refreshTokenDurationMs / 1000))
                .createdAt(LocalDateTime.now())
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken findByTokenOrThrow(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidRefreshTokenException("유효하지 않은 refresh token입니다."));
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new ExpiredRefreshTokenException("Refresh token이 만료되었습니다. 다시 로그인해주세요.");
        }
        return token;
    }

    @Transactional
    public void deleteByUserEmail(String userEmail) {
        refreshTokenRepository.deleteByUserEmail(userEmail);
    }

    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }
}