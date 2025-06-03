package da_ni_ni.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import java.time.Clock;

@Configuration
public class ClockConfig {

    // 기본 환경에서는 시스템 시계 사용
    @Bean
    @Profile("!test")
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    // 테스트 환경에서는 테스트에서 모킹할 수 있도록 Clock 제공
    @Bean
    @Profile("test")
    public Clock testClock() {
        return Clock.systemDefaultZone();
    }
}
