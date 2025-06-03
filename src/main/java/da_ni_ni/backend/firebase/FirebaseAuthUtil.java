package da_ni_ni.backend.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FirebaseAuthUtil {

    /**
     * Firebase ID 토큰 검증
     * @param idToken 클라이언트에서 받은 ID 토큰
     * @return 검증된 토큰 정보
     */
    public FirebaseToken verifyToken(String idToken) {
        try {
            return FirebaseAuth.getInstance().verifyIdToken(idToken);
        } catch (FirebaseAuthException e) {
            log.error("토큰 검증 실패: {}", e.getMessage(), e);
            throw new RuntimeException("Firebase ID 토큰 검증 실패", e);
        }
    }

    /**
     * 사용자 ID로 커스텀 토큰 생성
     * @param uid 사용자 ID
     * @return 생성된 커스텀 토큰
     */
    public String createCustomToken(String uid) {
        try {
            return FirebaseAuth.getInstance().createCustomToken(uid);
        } catch (FirebaseAuthException e) {
            log.error("커스텀 토큰 생성 실패: {}", e.getMessage(), e);
            throw new RuntimeException("Firebase 커스텀 토큰 생성 실패", e);
        }
    }
}