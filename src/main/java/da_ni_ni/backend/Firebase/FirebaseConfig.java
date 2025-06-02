package da_ni_ni.backend.Firebase;

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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Configuration
public class FirebaseConfig {

    /** 운영 환경(Render)에서만 설정해 두는 환경 변수 */
    @Value("${FIREBASE_SERVICE_ACCOUNT_JSON:}")
    private String firebaseServiceAccountJson;

    @PostConstruct
    public void initFirebase() throws IOException {
        InputStream serviceAccountStream;

        if (firebaseServiceAccountJson != null && !firebaseServiceAccountJson.isBlank()) {
            // 운영 환경: 환경 변수에서 받아온 JSON 문자열을 임시 파일로 만들기
            Path tempFilePath = Files.createTempFile("firebase-service-account-", ".json");
            Files.write(tempFilePath, firebaseServiceAccountJson.getBytes(StandardCharsets.UTF_8));
            tempFilePath.toFile().deleteOnExit();
            serviceAccountStream = new FileInputStream(tempFilePath.toFile());
            log.info("Firebase Credential: 운영 환경에서 임시 파일을 만들어 읽어옵니다.");
        } else {
            // 로컬 개발 환경: resources 폴더에서 직접 JSON 파일 읽기
            ClassPathResource resource = new ClassPathResource("danini-firebase-service-account.json");
            serviceAccountStream = resource.getInputStream();
            log.info("Firebase Credential: 로컬 resources 폴더에서 JSON 파일을 읽어옵니다.");
        }

        try (serviceAccountStream) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("Firebase 애플리케이션이 초기화되었습니다.");
            }
        } catch (IOException e) {
            log.error("Firebase 초기화 중 오류 발생: ", e);
            throw e;
        }
    }

    /** FirebaseMessaging 인스턴스를 빈으로 등록 */
    @Bean
    public FirebaseMessaging firebaseMessaging() {
        // initFirebase()에서 FirebaseApp이 초기화되었으므로, 바로 인스턴스를 반환할 수 있습니다.
        return FirebaseMessaging.getInstance();
    }
}
