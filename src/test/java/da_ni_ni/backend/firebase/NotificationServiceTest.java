package da_ni_ni.backend.firebase;

import da_ni_ni.backend.daily.domain.Comment;
import da_ni_ni.backend.daily.domain.Daily;
import da_ni_ni.backend.group.domain.FamilyGroup;
import da_ni_ni.backend.group.repository.GroupRepository;
import da_ni_ni.backend.qna.domain.DailyAnswer;
import da_ni_ni.backend.qna.domain.DailyQuestion;
import da_ni_ni.backend.qna.repository.DailyQuestionRepository;
import da_ni_ni.backend.user.domain.User;
import da_ni_ni.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * NotificationServiceTest
 *
 * • Mockito의 UnnecessaryStubbingException을 피하기 위해 클래스 전체를 Lenient 모드로 설정했습니다.
 *   (필요한 스텁만 엄격하게 사용하고, 나머지는 경고 없이 넘어가도록 처리)
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class NotificationServiceTest {

    private NotificationService notificationService;

    @org.mockito.Mock
    private UserRepository userRepository;

    @org.mockito.Mock
    private DailyQuestionRepository dailyQuestionRepository;

    @org.mockito.Mock
    private GroupRepository groupRepository;

    @org.mockito.Mock
    private FirebaseNotificationService firebaseNotificationService;

    // — 테스트에서 공통으로 사용할 도메인 객체들
    private User testUser;       // 알림을 받는 사용자 (댓글/답변의 대상)
    private User testUser2;      // 댓글/답변을 작성한 사용자
    private FamilyGroup testGroup;
    private Daily testDaily;
    private Comment testComment;
    private DailyQuestion testQuestion;
    private DailyAnswer testAnswer;

    @BeforeEach
    void setUp() {
        // NotificationService에 @InjectMocks 없이 직접 생성하여 의존성 주입
        notificationService = new NotificationService(firebaseNotificationService, userRepository, groupRepository, dailyQuestionRepository);

        // ─── 1) 그룹 멤버용 사용자(testUser) ───
        testUser = User.builder()
                .id(1L)
                .name("테스트 사용자")          // getName() → "테스트 사용자"
                .nickName("테스트사용자1")       // getNickName() → "테스트사용자1"
                .email("test@example.com")
                .fcmToken("test-fcm-token")
                .passwordHash("hashedPassword")
                .build();

        // ─── 2) 댓글/답변 작성자용 사용자(testUser2) ───
        testUser2 = User.builder()
                .id(2L)
                .name("테스트 사용자 2")        // getName() → "테스트 사용자 2"
                .nickName("테스트사용자2")       // getNickName() → "테스트사용자2"
                .email("test2@example.com")
                .fcmToken("test-fcm-token-2")
                .passwordHash("hashedPassword2")
                .build();

        // ─── 3) FamilyGroup ───
        testGroup = FamilyGroup.builder()
                .id(1L)
                .name("TestGroup")
                .build();

        // ─── 4) Daily (일기) ───
        testDaily = Daily.builder()
                .id(1L)
                .user(testUser)           // 일기 작성자 = testUser
                .familyGroup(testGroup)
                .content("오늘의 일기 내용")
                .date(LocalDate.now())
                .commentCount(0)
                .likeCount(0)
                .build();

        // ─── 5) Comment (댓글) ───
        testComment = Comment.builder()
                .commentId(1L)
                .content("테스트 댓글")
                .user(testUser2)         // 댓글 작성자 = testUser2
                .daily(testDaily)
                .build();

        // ─── 6) DailyQuestion (일일 질문) ───
        testQuestion = new DailyQuestion();
        testQuestion.setId(1L);
        testQuestion.setQuestion("오늘의 질문입니다.");
        testQuestion.setActivationDate(LocalDate.now());

        // ─── 7) DailyAnswer (일일 답변) ───
        testAnswer = new DailyAnswer();
        testAnswer.setId(1L);
        testAnswer.setQuestion(testQuestion);
        testAnswer.setUserId(testUser2.getId());       // 답변 작성자의 ID
        testAnswer.setAnswerText("테스트 답변입니다.");
        testAnswer.setCreatedAt(LocalDateTime.now());

        // ── 공통 Stub │ testUser2 찾기 ──
        // “answer.getUserId() → testUser2”
        lenient().when(userRepository.findById(testUser2.getId()))
                .thenReturn(Optional.of(testUser2));

        // ── 공통 Stub │ 그룹 내 멤버 조회 ──
        // “userRepository.findAllByFamilyGroup(testGroup) → [testUser]”
        // testUser2도 반드시 group에 속한다고 가정
        testUser2.setFamilyGroup(testGroup);
        testUser.setFamilyGroup(testGroup);
        lenient().when(userRepository.findAllByFamilyGroup(testGroup))
                .thenReturn(Collections.singletonList(testUser));

        // ── 공통 Stub │ 오늘의 질문 조회 ──
        // testSendDailyQuestionNotification에서만 실제 사용됨
        lenient().when(dailyQuestionRepository.findByActivationDate(any(LocalDate.class)))
                .thenReturn(Optional.of(testQuestion));

        // ── 공통 Stub │ 그룹 조회 (필요시) ──
        lenient().when(groupRepository.findById(testGroup.getId()))
                .thenReturn(Optional.of(testGroup));
    }

    // ────────────────────────────────────────────────────────────────────
    // 테스트 1: 일기에 새 댓글이 달렸을 때
    //   → NotificationService.sendDailyCommentNotification(...) 호출 시
    //   → FirebaseNotificationService.sendNotification(token, title, body, data) 검증
    // ────────────────────────────────────────────────────────────────────
    @Test
    void testSendDailyCommentNotification() {
        // given: setUp()에서 testComment, testDaily를 미리 준비함

        // when: 실제로 알림 로직 실행
        notificationService.sendDailyCommentNotification(testComment, testDaily);

        // then: FirebaseNotificationService.sendNotification(...)가 호출되었는지 검증
        // 제목(title) 은 "새로운 댓글"
        // body 에는 “댓글 작성자 이름(getName) + "님이 회원님의 일기에 댓글을 남겼습니다."” 가 포함되어야 함
        verify(firebaseNotificationService).sendNotification(
                eq(testUser.getFcmToken()),                          // token: testUser의 fcmToken
                eq("새로운 댓글"),                                     // title
                contains(testUser2.getName()),                        // body: “테스트 사용자 2님이 회원님의 일기에 댓글을 남겼습니다.”
                anyMap()                                              // data: 내부에서 생성하는 Map 객체 (commentId, notificationType, dailyId 등)
        );
    }

    // ────────────────────────────────────────────────────────────────────
    // 테스트 2: 매일 새로운 질문이 도착했을 때
    //   → NotificationService.sendNewDailyQuestionNotification() 호출 시
    //   → FirebaseNotificationService.sendNotificationToTopic(...) 검증
    // ────────────────────────────────────────────────────────────────────
    @Test
    void testSendNewDailyQuestionNotification() {
        // given: setUp()에서 dailyQuestionRepository.findByActivationDate(...)를 stub 처리

        // when: 실제로 알림 로직 실행
        notificationService.sendNewDailyQuestionNotification();

        // then: FirebaseNotificationService.sendNotificationToTopic(...)가 호출되었는지 검증
        verify(firebaseNotificationService).sendNotificationToTopic(
                eq("daily_question"),                                        // topic
                eq("오늘의 질문이 도착했어요!"),                              // title
                contains("새로운 가족 질문이 등록되었습니다"),                 // body: 적어도 이 문자열을 포함해야 함
                anyMap()                                                     // data: questionId, type 등의 Map
        );
    }

    // ────────────────────────────────────────────────────────────────────
    // 테스트 3: 일일 질문에 답변이 달렸을 때
    //   → NotificationService.sendDailyAnswerNotification(...) 호출 시
    //   → FirebaseNotificationService.sendMulticastNotification(...) 검증
    // ────────────────────────────────────────────────────────────────────
    @Test
    void testSendDailyAnswerNotification() {
        // given: setUp()에서 testAnswer, testQuestion, testUser2.findById(...) 등 stub 처리

        // when: 실제로 알림 로직 실행
        notificationService.sendDailyAnswerNotification(testAnswer, testQuestion);

        // then: FirebaseNotificationService.sendMulticastNotification(...)가 호출되었는지 검증
        // title은 "새로운 답변이 등록되었습니다"
        // body에는 “답변 작성자 닉네임(getNickName) + "님이 오늘의 질문에 답변했습니다."” 가 포함되어야 함
        verify(firebaseNotificationService).sendMulticastNotification(
                argThat(tokens -> tokens.contains(testUser.getFcmToken())), // tokens 목록에 testUser 토큰이 포함되어야 함
                eq("새로운 답변이 등록되었습니다"),                          // title
                contains(testUser2.getNickName()),                            // body: “테스트사용자2님이 오늘의 질문에 답변했습니다.”
                anyMap()                                                      // data: type, questionId, questionText, questionDate 등
        );
    }
}
