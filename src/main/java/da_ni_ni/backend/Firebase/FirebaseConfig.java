package da_ni_ni.backend.Firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Bean
    public FirebaseMessaging firebaseMessaging() throws IOException {
        // Firebase 서비스 계정 키 JSON 파일 경로
        // src/main/resources/firebase-service-account.json에 파일을 위치시킵니다
        ClassPathResource resource = new ClassPathResource("danini-firebase-service-account.json");

        try (InputStream serviceAccount = resource.getInputStream()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            // 이미 초기화된 앱이 있는지 확인
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("Firebase 애플리케이션이 초기화되었습니다.");
            }

            return FirebaseMessaging.getInstance();
        } catch (IOException e) {
            log.error("Firebase 초기화 중 오류 발생: ", e);
            throw e;
        }
    }
}