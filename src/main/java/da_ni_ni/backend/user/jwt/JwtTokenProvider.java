package da_ni_ni.backend.user.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final Key key;
    private final long accessTokenExpirationMs;
    private UserDetailsService userDetailsService;

    public JwtTokenProvider(JwtTokenProperties properties) {
        byte[] keyBytes = Base64.getUrlDecoder().decode(properties.getSecret());
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpirationMs = properties.getExpirationMs();
        log.info("Access Token 만료 시간 설정 값: {}", this.accessTokenExpirationMs);
    }

    // Access Token 생성
    public String createAccessToken(String username) {
        Claims claims = Jwts.claims().setSubject(username);
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpirationMs);

        log.info("Access Token 만료 시간: {}", expiry);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 기존 createToken 메서드는 backward compatibility를 위해 유지
    public String createToken(String username) {
        return createAccessToken(username);
    }

    public String getEmail(String token) {
        return Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public Authentication getAuthentication(String token) {
        String email = getEmail(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.warn("[JwtTokenProvider] 토큰 검증 실패: " + e.getMessage());
            return false;
        }
    }

    // 토큰 만료 여부 확인 (만료되어도 예외 던지지 않음)
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true; // 파싱 실패 시 만료된 것으로 간주
        }
    }
}
