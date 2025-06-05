package da_ni_ni.backend.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${FIREBASE_SERVICE_ACCOUNT_JSON:}")
    private String firebaseServiceAccountJson;

    @PostConstruct
    public void initFirebase() throws IOException {
        log.info("[FirebaseConfig.initFirebase] 진입 → FIREBASE_SERVICE_ACCOUNT_JSON={}", firebaseServiceAccountJson);

        InputStream serviceAccountStream;
        if (firebaseServiceAccountJson != null && !firebaseServiceAccountJson.isEmpty()) {
            // 환경 변수에 인코딩된 JSON 문자열이 들어왔나 확인
            if (firebaseServiceAccountJson.startsWith("-----BEGIN")) {
                byte[] decoded = Base64.getDecoder().decode(firebaseServiceAccountJson);
                serviceAccountStream = new ByteArrayInputStream(decoded);
                log.info("[FirebaseConfig] Base64 인코딩된 JSON 스트림으로 로드 완료");
            } else {
                // classpath:xxx 형태인지 확인
                log.info("[FirebaseConfig] ClassPathResource 로드 시도 → path={}", firebaseServiceAccountJson);
                ClassPathResource resource = new ClassPathResource(firebaseServiceAccountJson);
                log.info("[FirebaseConfig] ClassPathResource.exists()={}", resource.exists());
                serviceAccountStream = resource.getInputStream();
                log.info("[FirebaseConfig] ClassPathResource 로드 성공 → {}", firebaseServiceAccountJson);
            }
        } else {
            // 로컬 개발용 루트 리소스 경로
            log.info("[FirebaseConfig] 로컬 resources에서 JSON 로드 시도");
            ClassPathResource resource = new ClassPathResource("danini-firebase-service-account.json");
            log.info("[FirebaseConfig] 로컬 JSON 파일 존재 여부: {}", resource.exists());
            serviceAccountStream = resource.getInputStream();
            log.info("[FirebaseConfig] 로컬 서비스 계정 JSON 로드 완료");
        }

        try {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("[FirebaseConfig] Firebase 애플리케이션 초기화 완료");
            }
        } catch (Exception e) {
            log.error("[FirebaseConfig] Firebase 초기화 중 예외 발생", e);
            throw e;  // 프로그램 구동 중단(BeanCreationException) → 스택트레이스가 로그에 찍힘
        }
    }

    @Bean
    public FirebaseMessaging firebaseMessaging() {
        return FirebaseMessaging.getInstance();
    }
}
