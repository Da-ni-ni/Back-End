
package da_ni_ni.backend.user.jwt;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtTokenProperties {

    private String secret;
    private long expirationMs;

    @PostConstruct
    public void init() {
        System.out.println("JWT Secret: " + secret);
    }// 디버깅 로그 추가

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public void setExpirationMs(long expirationMs) {
        this.expirationMs = expirationMs;
    }
}
