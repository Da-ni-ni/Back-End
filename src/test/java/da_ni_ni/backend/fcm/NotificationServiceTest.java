package da_ni_ni.backend.fcm;

import da_ni_ni.backend.Firebase.FirebaseNotificationService;
import da_ni_ni.backend.Firebase.NotificationService;
import da_ni_ni.backend.group.domain.FamilyGroup;
import da_ni_ni.backend.group.repository.GroupRepository;
import da_ni_ni.backend.qna.domain.DailyAnswer;
import da_ni_ni.backend.qna.domain.DailyQuestion;
import da_ni_ni.backend.user.domain.User;
import da_ni_ni.backend.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @MockBean
    private FirebaseNotificationService firebaseNotificationService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private GroupRepository groupRepository;

    @Test
    void testSendDailyAnswerNotification() {
        // 테스트 데이터 준비
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .nickName("테스터")
                .fcmToken("test-fcm-token")
                .build();

        // Builder 패턴을 사용하여 FamilyGroup 생성
        FamilyGroup group = FamilyGroup.builder()
                .id(1L)
                .name("테스트 그룹")
                .inviteCode("TEST123")
                .build();

        user.setFamilyGroup(group);

        User otherUser = User.builder()
                .id(2L)
                .email("other@example.com")
                .nickName("다른사용자")
                .fcmToken("other-fcm-token")
                .familyGroup(group)
                .build();

        DailyAnswer answer = new DailyAnswer();
        answer.setUserId(user.getId());

        DailyQuestion question = new DailyQuestion();
        question.setId(1L);
        question.setQuestion("오늘의 질문");
        question.setActivationDate(LocalDate.now());

        // Mock 설정
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findAllByFamilyGroup(group)).thenReturn(List.of(user, otherUser));

        // 메소드 실행
        notificationService.sendDailyAnswerNotification(answer, question);

        // 검증
        verify(firebaseNotificationService).sendMulticastNotification(
                eq(List.of("other-fcm-token")),
                eq("새로운 답변이 등록되었습니다"),
                eq("테스터님이 오늘의 질문에 답변했습니다."),
                any()
        );
    }

}
