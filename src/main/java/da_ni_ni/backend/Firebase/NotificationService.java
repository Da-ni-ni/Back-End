package da_ni_ni.backend.Firebase;

import da_ni_ni.backend.daily.domain.Comment;
import da_ni_ni.backend.daily.domain.Daily;
import da_ni_ni.backend.group.domain.FamilyGroup;
import da_ni_ni.backend.group.repository.GroupRepository;
import da_ni_ni.backend.qna.domain.DailyAnswer;
import da_ni_ni.backend.qna.domain.DailyQuestion;
import da_ni_ni.backend.qna.repository.DailyQuestionRepository;
import da_ni_ni.backend.user.domain.User;
import da_ni_ni.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final FirebaseNotificationService firebaseNotificationService;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final DailyQuestionRepository dailyQuestionRepository;

    /**
     * 1. 내 Daily에 댓글이 달렸을 때 알림
     */
    public void sendDailyCommentNotification(Comment comment, Daily daily) {
        // Daily 작성자 정보 가져오기
        User dailyOwner = daily.getUser();
        String ownerToken = dailyOwner.getFcmToken();

        if (ownerToken == null || ownerToken.isEmpty()) {
            log.warn("Daily 작성자({})의 FCM 토큰이 존재하지 않습니다.", dailyOwner.getId());
            return;
        }

        // 댓글 작성자와 Daily 작성자가 같은 경우 알림 전송하지 않음
        if (comment.getUser().getId().equals(dailyOwner.getId())) {
            return;
        }

        // 알림 데이터 구성
        String commentAuthorName = comment.getUser().getName();
        String title = "새로운 댓글";
        String body = commentAuthorName + "님이 회원님의 일기에 댓글을 남겼습니다.";

        Map<String, String> data = new HashMap<>();
        data.put("notificationType", "DAILY_COMMENT");
        data.put("dailyId", daily.getId().toString());
        data.put("commentId", comment.getCommentId().toString());

        // 알림 전송
        firebaseNotificationService.sendNotification(ownerToken, title, body, data);
    }

    /**
     * 2. 그룹 구성원의 감정이 변했을 때 알림
     */
    public void sendEmotionChangeNotification(User user, String previousEmotion, String newEmotion) {
        FamilyGroup group = user.getFamilyGroup();
        if (group == null) {
            log.warn("사용자({})가 속한 그룹이 없습니다.", user.getId());
            return;
        }

        String userName = user.getName();

        // 같은 그룹의 다른 구성원들에게 알림
        List<String> targetTokens = group.getUsers().stream()
                .filter(m -> !m.getId().equals(user.getId())) // 감정이 변한 본인 제외
                .map(User::getFcmToken)
                .filter(token -> token != null && !token.isEmpty())
                .collect(Collectors.toList());

        if (targetTokens.isEmpty()) {
            log.warn("알림을 보낼 그룹 구성원이 없습니다.");
            return;
        }

        // 알림 데이터 구성
        String title = "그룹원 감정 변화";
        String body = userName + "님의 감정이 " + previousEmotion + "에서 " + newEmotion + "(으)로 변화했습니다.";

        Map<String, String> data = new HashMap<>();
        data.put("notificationType", "EMOTION_CHANGE");
        data.put("userId", user.getId().toString());
        data.put("previousEmotion", previousEmotion);
        data.put("newEmotion", newEmotion);

        // 알림 전송
        firebaseNotificationService.sendMulticastNotification(targetTokens, title, body, data);
    }

    /**
     * 3. 일일 문답에 다른 그룹원이 답변을 달았을 때 알림
     */
    public void sendDailyAnswerNotification(DailyAnswer answer, DailyQuestion question) {
        // answer.getUser() 대신 userRepository 사용
        User answerUser = userRepository.findById(answer.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + answer.getUserId()));

        // GroupRepository의 findByUserId 대신 User에서 직접 그룹 정보 가져오기
        FamilyGroup group = answerUser.getFamilyGroup();
        if (group == null) {
            log.warn("사용자 {}는 그룹에 속해 있지 않습니다.", answerUser.getId());
            return;
        }

        // 그룹에 속한 모든 사용자 가져오기
        List<User> groupMembers = userRepository.findAllByFamilyGroup(group);

        List<String> tokens = groupMembers.stream()
                .filter(member -> !member.getId().equals(answerUser.getId())) // 답변 작성자 제외
                .map(User::getFcmToken)
                .filter(token -> token != null && !token.isEmpty())
                .collect(Collectors.toList());

        if (!tokens.isEmpty()) {
            Map<String, String> data = new HashMap<>();
            data.put("type", "DAILY_ANSWER");
            data.put("questionId", question.getId().toString());
            data.put("questionText", question.getQuestion()); // getContent() 대신 getQuestion() 사용
            data.put("questionDate", question.getActivationDate().toString()); // getDate() 대신 getActivationDate() 사용

            firebaseNotificationService.sendMulticastNotification(
                    tokens,
                    "새로운 답변이 등록되었습니다",
                    // getNickname() 대신 getNickName() 사용
                    answerUser.getNickName() + "님이 오늘의 질문에 답변했습니다.",
                    data
            );
        }
    }

    /**
     * 4. 신규 가입자가 초대 코드 입력시 그룹 생성자에게 알림
     */
    public void sendGroupJoinNotification(User newUser, User adminUser) {
        String adminToken = adminUser.getFcmToken();

        if (adminToken == null || adminToken.isEmpty()) {
            log.warn("그룹 관리자({})의 FCM 토큰이 존재하지 않습니다.", adminUser.getId());
            return;
        }

        // 알림 데이터 구성
        String title = "새로운 그룹 구성원";
        String body = newUser.getName() + "님이 가족에 합류하고자 합니다. 마이페이지를 확인해주세요.";

        Map<String, String> data = new HashMap<>();
        data.put("notificationType", "GROUP_JOIN");
        data.put("newUserId", newUser.getId().toString());
        data.put("groupId", adminUser.getFamilyGroup().getId().toString());

        // 알림 전송
        firebaseNotificationService.sendNotification(adminToken, title, body, data);
    }
    /**
     * 5. 오늘의 새로운 질문이 생성되었을 때 알림
     * 스케줄러에서 호출하기 위한 메서드
     */
    public void sendNewDailyQuestionNotification() {
        try {
            LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
            Optional<DailyQuestion> questionOpt = dailyQuestionRepository.findByActivationDate(today);

            if (questionOpt.isEmpty()) {
                log.warn("오늘({})의 질문이 없어 푸시 알림을 보낼 수 없습니다.", today);
                return;
            }

            DailyQuestion todayQuestion = questionOpt.get();

            String topic = "daily_question"; // 클라이언트가 구독해야 하는 토픽 이름
            String title = "오늘의 질문이 도착했어요!";
            String body = "새로운 가족 질문이 등록되었습니다. 여러분의 생각을 가족과 나눠보세요.";

            Map<String, String> data = new HashMap<>();
            data.put("questionId", todayQuestion.getId().toString());
            data.put("type", "NEW_QUESTION");

            String messageId = firebaseNotificationService.sendNotificationToTopic(topic, title, body, data);
            log.info("오늘의 질문 알림 전송 완료: messageId={}, questionId={}", messageId, todayQuestion.getId());
        } catch (Exception e) {
            log.error("오늘의 질문 알림 전송 중 오류 발생", e);
        }
    }
}