package da_ni_ni.backend.refreshtoken;

import da_ni_ni.backend.user.jwt.JwtTokenProperties;
import da_ni_ni.backend.user.jwt.JwtTokenProvider;
import da_ni_ni.backend.user.service.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private JwtTokenProperties jwtTokenProperties;

    private JwtTokenProvider jwtTokenProvider;
    private String testSecret;
    private Key testKey;
    private long testExpirationMs;

    @BeforeEach
    void setUp() {
        testSecret = Base64.getUrlEncoder().encodeToString("test-secret-key-for-jwt-token-generation-and-validation".getBytes());
        testExpirationMs = 3600000L; // 1시간

        when(jwtTokenProperties.getSecret()).thenReturn(testSecret);
        when(jwtTokenProperties.getExpirationMs()).thenReturn(testExpirationMs);

        jwtTokenProvider = new JwtTokenProvider(jwtTokenProperties);

        // userDetailsService를 주입
        ReflectionTestUtils.setField(jwtTokenProvider, "userDetailsService", userDetailsService);

        byte[] keyBytes = Base64.getUrlDecoder().decode(testSecret);
        testKey = Keys.hmacShaKeyFor(keyBytes);
    }

    @Test
    @DisplayName("Access Token을 성공적으로 생성한다")
    void createAccessTokenShouldGenerateValidToken() {
        // given
        String username = "test@example.com";

        // when
        String token = jwtTokenProvider.createAccessToken(username);

        // then
        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();

        // 토큰을 파싱하여 내용 검증
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(testKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertThat(claims.getSubject()).isEqualTo(username);
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isNotNull();
        assertThat(claims.getExpiration()).isAfter(new Date());
    }

    @Test
    @DisplayName("backward compatibility를 위한 createToken 메서드가 정상 작동한다")
    void createTokenShouldWorkForBackwardCompatibility() {
        // given
        String username = "test@example.com";

        // when
        String token = jwtTokenProvider.createToken(username);

        // then
        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();

        String email = jwtTokenProvider.getEmail(token);
        assertThat(email).isEqualTo(username);
    }

    @Test
    @DisplayName("토큰에서 이메일을 정상적으로 추출한다")
    void getEmailShouldExtractEmailFromToken() {
        // given
        String username = "test@example.com";
        String token = jwtTokenProvider.createAccessToken(username);

        // when
        String extractedEmail = jwtTokenProvider.getEmail(token);

        // then
        assertThat(extractedEmail).isEqualTo(username);
    }

    @Test
    @DisplayName("유효한 토큰에 대해 true를 반환한다")
    void validateTokenShouldReturnTrueForValidToken() {
        // given
        String username = "test@example.com";
        String token = jwtTokenProvider.createAccessToken(username);

        // when
        boolean isValid = jwtTokenProvider.validateToken(token);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("유효하지 않은 토큰에 대해 false를 반환한다")
    void validateTokenShouldReturnFalseForInvalidToken() {
        // given
        String invalidToken = "invalid.jwt.token";

        // when
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("만료된 토큰에 대해 false를 반환한다")
    void validateTokenShouldReturnFalseForExpiredToken() {
        // given - 만료된 토큰 생성
        String username = "test@example.com";
        Date pastDate = new Date(System.currentTimeMillis() - 1000); // 1초 전

        String expiredToken = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis() - 2000))
                .setExpiration(pastDate)
                .signWith(testKey)
                .compact();

        // when
        boolean isValid = jwtTokenProvider.validateToken(expiredToken);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("토큰으로부터 Authentication 객체를 생성한다")
    void getAuthenticationShouldCreateAuthenticationObject() {
        // given
        String username = "test@example.com";
        String token = jwtTokenProvider.createAccessToken(username);

        UserDetails userDetails = new User(
                username,
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);

        // when
        Authentication authentication = jwtTokenProvider.getAuthentication(token);

        // then
        assertThat(authentication).isNotNull();
        assertThat(authentication.getName()).isEqualTo(username);
        assertThat(authentication.getAuthorities()).hasSize(1);
        assertThat(authentication.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("유효한 토큰이 만료되지 않았다고 판단한다")
    void isTokenExpiredShouldReturnFalseForValidToken() {
        // given
        String username = "test@example.com";
        String token = jwtTokenProvider.createAccessToken(username);

        // when
        boolean isExpired = jwtTokenProvider.isTokenExpired(token);

        // then
        assertThat(isExpired).isFalse();
    }

    @Test
    @DisplayName("만료된 토큰을 만료되었다고 판단한다")
    void isTokenExpiredShouldReturnTrueForExpiredToken() {
        // given - 만료된 토큰 생성
        String username = "test@example.com";
        Date pastDate = new Date(System.currentTimeMillis() - 1000); // 1초 전

        String expiredToken = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis() - 2000))
                .setExpiration(pastDate)
                .signWith(testKey)
                .compact();

        // when
        boolean isExpired = jwtTokenProvider.isTokenExpired(expiredToken);

        // then
        assertThat(isExpired).isTrue();
    }

    @Test
    @DisplayName("유효하지 않은 토큰을 만료된 것으로 간주한다")
    void isTokenExpiredShouldReturnTrueForInvalidToken() {
        // given
        String invalidToken = "invalid.jwt.token";

        // when
        boolean isExpired = jwtTokenProvider.isTokenExpired(invalidToken);

        // then
        assertThat(isExpired).isTrue();
    }

    @Test
    @DisplayName("토큰 만료 시간이 올바르게 설정된다")
    void tokenExpirationShouldBeSetCorrectly() {
        // given
        String username = "test@example.com";
        long currentTime = System.currentTimeMillis();

        // when
        String token = jwtTokenProvider.createAccessToken(username);

        // then
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(testKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        long tokenExpiration = claims.getExpiration().getTime();
        long expectedExpiration = currentTime + testExpirationMs;

        // 약간의 시간 차이는 허용 (1초)
        assertThat(Math.abs(tokenExpiration - expectedExpiration)).isLessThan(1000);
    }
}