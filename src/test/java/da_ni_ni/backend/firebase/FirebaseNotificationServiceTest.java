package da_ni_ni.backend.firebase;

import com.google.api.core.ApiFuture;
import com.google.firebase.messaging.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class FirebaseNotificationServiceTest {

    @Mock
    private FirebaseMessaging firebaseMessaging;

    @InjectMocks
    private FirebaseNotificationService firebaseNotificationService;

    private Map<String, String> testData;
    private String testToken;
    private List<String> testTokens;
    private String testTopic;

    @BeforeEach
    public void setup() {
        testData = new HashMap<>();
        testData.put("key1", "value1");
        testData.put("key2", "value2");

        testToken = "test-firebase-token";
        testTokens = Arrays.asList("token1", "token2", "token3");
        testTopic = "test-topic";
    }

    @Test
    public void testSendNotification() throws Exception {
        // given
        String expectedMessageId = "message-id-123";

        // ApiFuture 모킹
        ApiFuture<String> mockFuture = mock(ApiFuture.class);
        when(mockFuture.get()).thenReturn(expectedMessageId);

        // sendAsync 메서드 모킹
        when(firebaseMessaging.sendAsync(any(Message.class))).thenReturn(mockFuture);

        // when
        String result = firebaseNotificationService.sendNotification(
                testToken, "테스트 제목", "테스트 내용", testData);

        // then
        assertEquals(expectedMessageId, result);
        verify(firebaseMessaging, times(1)).sendAsync(any(Message.class));
    }

    @Test
    public void testSendMulticastNotification() throws Exception {
        // given
        BatchResponse mockResponse = mock(BatchResponse.class);
        when(mockResponse.getSuccessCount()).thenReturn(2);
        when(mockResponse.getFailureCount()).thenReturn(1);

        when(firebaseMessaging.sendMulticast(any(MulticastMessage.class))).thenReturn(mockResponse);

        // when
        BatchResponse result = firebaseNotificationService.sendMulticastNotification(
                testTokens, "멀티캐스트 제목", "멀티캐스트 내용", testData);

        // then
        assertEquals(2, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());

        verify(firebaseMessaging, times(1)).sendMulticast(any(MulticastMessage.class));
    }

    @Test
    public void testSendNotificationToTopic() throws Exception {
        // given
        String expectedMessageId = "topic-message-id-123";
        when(firebaseMessaging.send(any(Message.class))).thenReturn(expectedMessageId);

        // when
        String result = firebaseNotificationService.sendNotificationToTopic(
                testTopic, "토픽 제목", "토픽 내용", testData);

        // then
        assertEquals(expectedMessageId, result);

        verify(firebaseMessaging, times(1)).send(any(Message.class));
    }

    @Test
    public void testHandleFirebaseException() throws Exception {
        // given
        // FirebaseMessagingException을 모킹
        FirebaseMessagingException mockException = mock(FirebaseMessagingException.class);
        when(mockException.getMessage()).thenReturn("테스트 예외 메시지");

        when(firebaseMessaging.send(any(Message.class))).thenThrow(mockException);

        // when & then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            firebaseNotificationService.sendNotification(testToken, "제목", "내용", testData);
        });

        assertTrue(exception.getCause() instanceof FirebaseMessagingException);
        verify(firebaseMessaging, times(1)).send(any(Message.class));
    }
}