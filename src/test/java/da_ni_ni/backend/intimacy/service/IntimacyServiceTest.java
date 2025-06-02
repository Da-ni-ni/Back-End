package da_ni_ni.backend.intimacy.service;

import da_ni_ni.backend.group.domain.FamilyGroup;
import da_ni_ni.backend.group.repository.GroupRepository;
import da_ni_ni.backend.intimacy.domain.IntimacyScore;
import da_ni_ni.backend.intimacy.domain.IntimacyTestResponse;
import da_ni_ni.backend.intimacy.dto.AnswerDto;
import da_ni_ni.backend.intimacy.dto.FamilyScoreResponse;
import da_ni_ni.backend.intimacy.dto.PersonalScoreResponse;
import da_ni_ni.backend.intimacy.exception.GroupNotFoundException;
import da_ni_ni.backend.intimacy.exception.IntimacyRecordNotFoundException;
import da_ni_ni.backend.intimacy.exception.InvalidAnswerCountException;
import da_ni_ni.backend.intimacy.repository.IntimacyScoreRepository;
import da_ni_ni.backend.intimacy.repository.IntimacyTestResponseRepository;
import da_ni_ni.backend.user.domain.User;
import da_ni_ni.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IntimacyServiceTest {

    @Mock
    private IntimacyScoreRepository scoreRepo;

    @Mock
    private IntimacyTestResponseRepository respRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private GroupRepository groupRepo;

    @InjectMocks
    private IntimacyService intimacyService;

    private User testUser;
    private FamilyGroup testGroup;
    private List<AnswerDto> validAnswers;
    private IntimacyScore testScore;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 설정
        testUser = User.builder()
                .id(1L)
                .name("테스트사용자")
                .build();

        // 테스트용 그룹 설정
        testGroup = FamilyGroup.builder()
                .id(1L)
                .name("테스트가족")
                .build();
        testUser.setFamilyGroup(testGroup);

        // 유효한 답변 리스트 생성 (10개 항목, 각 값은 1~5)
        validAnswers = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            validAnswers.add(new AnswerDto(i, i % 5 + 1));
        }

        // 테스트용 점수 객체 설정
        testScore = new IntimacyScore();
        testScore.setIntimacyId(1);
        testScore.setUser(testUser);
        testScore.setScore(60); // 예시 점수
        testScore.setTestDate(LocalDate.now());
    }

    @Test
    @DisplayName("유효한 답변을 제출하면 점수와 응답이 저장됨")
    void submitAnswers_WithValidAnswers_SavesScoreAndResponse() {
        // Given
        when(scoreRepo.save(any(IntimacyScore.class))).thenReturn(testScore);
        when(respRepo.save(any(IntimacyTestResponse.class))).thenReturn(new IntimacyTestResponse());

        // When
        intimacyService.submitAnswers(testUser, validAnswers);

        // Then
        verify(scoreRepo, times(1)).save(any(IntimacyScore.class));
        verify(respRepo, times(1)).save(any(IntimacyTestResponse.class));
    }

    @Test
    @DisplayName("답변이는 null이면 예외 발생")
    void submitAnswers_WithNullAnswers_ThrowsException() {
        // When & Then
        assertThrows(InvalidAnswerCountException.class, () -> {
            intimacyService.submitAnswers(testUser, null);
        });
    }

    @Test
    @DisplayName("답변 개수가 10개가 아니면 예외 발생")
    void submitAnswers_WithInvalidAnswerCount_ThrowsException() {
        // Given
        List<AnswerDto> invalidAnswers = validAnswers.subList(0, 9); // 9개만 사용

        // When & Then
        assertThrows(InvalidAnswerCountException.class, () -> {
            intimacyService.submitAnswers(testUser, invalidAnswers);
        });
    }

    @Test
    @DisplayName("개인 점수 조회 성공")
    void getPersonalScore_ReturnsCorrectScore() {
        // Given
        when(scoreRepo.findFirstByUserOrderByTestDateDesc(testUser)).thenReturn(Optional.of(testScore));

        // When
        PersonalScoreResponse response = intimacyService.getPersonalScore(testUser);

        // Then
        assertEquals("나", response.getMemberName());
        assertEquals(60, response.getScore());
        verify(scoreRepo, times(1)).findFirstByUserOrderByTestDateDesc(testUser);
    }

    @Test
    @DisplayName("개인 점수 기록이 없을 때 예외 발생")
    void getPersonalScore_WithNoRecord_ThrowsException() {
        // Given
        when(scoreRepo.findFirstByUserOrderByTestDateDesc(testUser)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IntimacyRecordNotFoundException.class, () -> {
            intimacyService.getPersonalScore(testUser);
        });
    }

    @Test
    @DisplayName("가족 점수 조회 성공")
    void getFamilyScore_ReturnsCorrectAverageScore() {
        // Given
        User user1 = User.builder().id(1L).build();
        User user2 = User.builder().id(2L).build();
        User user3 = User.builder().id(3L).build();

        List<User> familyMembers = Arrays.asList(user1, user2, user3);

        IntimacyScore score1 = new IntimacyScore();
        score1.setScore(60);
        IntimacyScore score2 = new IntimacyScore();
        score2.setScore(80);
        IntimacyScore score3 = new IntimacyScore();
        score3.setScore(70);

        when(userRepo.findAllByFamilyGroup(testGroup)).thenReturn(familyMembers);
        when(scoreRepo.findFirstByUserOrderByTestDateDesc(user1)).thenReturn(Optional.of(score1));
        when(scoreRepo.findFirstByUserOrderByTestDateDesc(user2)).thenReturn(Optional.of(score2));
        when(scoreRepo.findFirstByUserOrderByTestDateDesc(user3)).thenReturn(Optional.of(score3));

        // When
        FamilyScoreResponse response = intimacyService.getFamilyScore(testUser);

        // Then
        assertEquals("테스트가족", response.getFamilyName());
        assertEquals(70.0, response.getAverageScore()); // (60 + 80 + 70) / 3 = 70.0
        verify(userRepo, times(1)).findAllByFamilyGroup(testGroup);
    }


    @Test
    @DisplayName("그룹이 없을 때 예외 발생")
    void getFamilyScore_WithNoGroup_ThrowsException() {
        // Given
        User userWithoutGroup = User.builder()
                .id(1L)
                .build();
        // familyGroup은 null로 설정됨

        // When & Then
        assertThrows(GroupNotFoundException.class, () -> {
            intimacyService.getFamilyScore(userWithoutGroup);
        });
    }

    @Test
    @DisplayName("일부 가족 구성원의 점수 기록이 없는 경우 평균 계산 확인")
    void getFamilyScore_WithPartialScores_CalculatesAverage() {
        // Given
        User user1 = User.builder().id(1L).build();
        User user2 = User.builder().id(2L).build();

        List<User> familyMembers = Arrays.asList(user1, user2);

        IntimacyScore score1 = new IntimacyScore();
        score1.setScore(90);

        when(userRepo.findAllByFamilyGroup(testGroup)).thenReturn(familyMembers);
        when(scoreRepo.findFirstByUserOrderByTestDateDesc(user1)).thenReturn(Optional.of(score1));
        when(scoreRepo.findFirstByUserOrderByTestDateDesc(user2)).thenReturn(Optional.empty()); // 기록 없음

        // When
        FamilyScoreResponse response = intimacyService.getFamilyScore(testUser);

        // Then
        assertEquals("테스트가족", response.getFamilyName());
        assertEquals(45.0, response.getAverageScore()); // (90 + 0) / 2 = 45.0
    }
}