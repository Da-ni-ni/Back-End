package da_ni_ni.backend.user;

import da_ni_ni.backend.user.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider();
        // application.yml 설정값 그대로 주입
        ReflectionTestUtils.setField(provider, "secretKey", "ChangeThisToASecureRandomString123!");
        ReflectionTestUtils.setField(provider, "validityInMs", 3600000L);
        provider.init();
    }

    @Test
    @DisplayName("토큰 생성 후 검증 및 이메일 추출")
    void createAndValidateToken() {
        String token = provider.createToken("test@example.com");

        assertTrue(provider.validateToken(token), "생성한 토큰은 유효해야 한다");
        assertEquals("test@example.com", provider.getEmail(token));
    }

    @Test
    @DisplayName("잘못된 토큰 검증 시 false 반환")
    void invalidToken() {
        assertFalse(provider.validateToken("this.is.not.valid"));
    }
}
