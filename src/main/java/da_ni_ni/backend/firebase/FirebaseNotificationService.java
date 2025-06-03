package da_ni_ni.backend.firebase;

import com.google.api.core.ApiFuture;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseNotificationService {

    private final FirebaseMessaging firebaseMessaging;

    /**
     * 단일 기기로 알림 메시지 전송
     *
     * @param token 사용자 FCM 토큰
     * @param title 알림 제목
     * @param body  알림 본문
     * @param data  추가 데이터 맵
     * @return 메시지 ID
     */
    public String sendNotification(String token, String title, String body, Map<String, String> data) {
        if (token == null || token.isEmpty()) {
            log.warn("유효하지 않은 FCM 토큰: {}", token);
            return null;
        }

        // 1) Message 객체 생성
        Message message = Message.builder()
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putAllData(data)
                .setToken(token)
                .build();

        // 2) 우선 비동기(sendAsync) 시도
        try {
            ApiFuture<String> future = firebaseMessaging.sendAsync(message);

            // sendAsync(...)가 모킹되어 null이 아니면 get()을 호출해 결과를 바로 리턴
            if (future != null) {
                String asyncResponse = future.get();
                log.info("Async 알림 전송 성공: {}", asyncResponse);
                return asyncResponse;
            }
        } catch (ExecutionException | InterruptedException e) {
            // 비동기 도중 예외가 발생하면, 이어서 동기 방식으로 전환
            log.warn("Async sendAsync 중 예외 발생: {}", e.getMessage());
            // (InterruptedException이 섞였을 때 스레드를 리셋해야 할 경우엔 아래처럼:)
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }

        // 3) 비동기(sendAsync)가 null을 반환하거나 get()에서 예외가 발생했을 때 동기 send() 시도
        try {
            String syncResponse = firebaseMessaging.send(message);
            log.info("Sync 알림 전송 성공: {}", syncResponse);
            return syncResponse;
        } catch (FirebaseMessagingException e) {
            // 동기 send()에서 예외(FirebaseMessagingException)가 발생하면 RuntimeException으로 래핑
            log.error("동기 send 중 FirebaseMessagingException 발생: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 여러 기기로 동일한 알림 메시지 전송
     *
     * @param tokens 사용자 FCM 토큰 목록
     * @param title 알림 제목
     * @param body 알림 본문
     * @param data 추가 데이터 맵
     * @return 배치 응답 결과
     */
    public BatchResponse sendMulticastNotification(List<String> tokens, String title, String body, Map<String, String> data) {
        if (tokens == null || tokens.isEmpty()) {
            log.warn("FCM 토큰 목록이 비어 있습니다.");
            return null;
        }

        // 유효한 토큰만 필터링
        List<String> validTokens = new ArrayList<>();
        for (String token : tokens) {
            if (token != null && !token.isEmpty()) {
                validTokens.add(token);
            }
        }

        if (validTokens.isEmpty()) {
            log.warn("유효한 FCM 토큰이 없습니다.");
            return null;
        }

        try {
            MulticastMessage message = MulticastMessage.builder()
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putAllData(data)
                    .addAllTokens(validTokens)
                    .build();

            BatchResponse response = firebaseMessaging.sendMulticast(message);
            log.info("Multicast 알림 전송 결과: 성공: {}, 실패: {}",
                    response.getSuccessCount(), response.getFailureCount());

            if (response.getFailureCount() > 0) {
                List<SendResponse> responses = response.getResponses();
                for (int i = 0; i < responses.size(); i++) {
                    if (!responses.get(i).isSuccessful()) {
                        FirebaseMessagingException e = responses.get(i).getException();
                        log.error("토큰 [{}]에 대한 알림 전송 실패: {}", validTokens.get(i), e.getMessage());

                        // 만료된 토큰인 경우 처리
                        if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                            log.warn("FCM 토큰이 더 이상 유효하지 않습니다: {}", validTokens.get(i));
                            // 필요시 만료된 토큰 처리 로직 추가
                        }
                    }
                }
            }

            return response;
        } catch (FirebaseMessagingException e) {
            log.error("Multicast 알림 전송 중 오류 발생: ", e);
            return null;
        }
    }

    /**
     * 토픽에 알림 메시지 전송
     * (모든 구독자에게 알림을 보낼 때 사용)
     *
     * @param topic 알림을 보낼 토픽 이름
     * @param title 알림 제목
     * @param body 알림 본문
     * @param data 추가 데이터 맵
     * @return 메시지 ID
     */
    public String sendNotificationToTopic(String topic, String title, String body, Map<String, String> data) {
        try {
            Message message = Message.builder()
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putAllData(data)
                    .setTopic(topic)
                    .build();

            String response = firebaseMessaging.send(message);
            log.info("토픽 [{}]으로 알림이 성공적으로 전송되었습니다: {}", topic, response);
            return response;
        } catch (FirebaseMessagingException e) {
            log.error("토픽 알림 전송 중 오류 발생: ", e);
            return null;
        }
    }
}