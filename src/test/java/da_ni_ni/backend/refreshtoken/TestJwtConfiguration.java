package da_ni_ni.backend.refreshtoken;

import da_ni_ni.backend.user.jwt.JwtTokenProperties;
import da_ni_ni.backend.user.jwt.JwtTokenProvider;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestJwtConfiguration {
    @Bean
    @Primary
    public JwtTokenProperties jwtTokenProperties() {
        JwtTokenProperties properties = new JwtTokenProperties();
        properties.setSecret("테스트용시크릿키입니다");
        properties.setExpirationMs(600000); // 10분으로 설정
        return properties;
    }

    @Bean
    @Primary
    public JwtTokenProvider jwtTokenProvider(JwtTokenProperties jwtTokenProperties) {
        return new JwtTokenProvider(jwtTokenProperties);
    }
}