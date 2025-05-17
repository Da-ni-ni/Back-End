package da_ni_ni.backend.intimacy.service;

import da_ni_ni.backend.intimacy.dto.AnswerDto;
import da_ni_ni.backend.intimacy.dto.FamilyScoreResponse;
import da_ni_ni.backend.intimacy.dto.PersonalScoreResponse;
import da_ni_ni.backend.intimacy.exception.GroupNotFoundException;
import da_ni_ni.backend.intimacy.exception.IntimacyRecordNotFoundException;
import da_ni_ni.backend.group.domain.FamilyGroup;
import da_ni_ni.backend.intimacy.domain.IntimacyScore;
import da_ni_ni.backend.intimacy.repository.IntimacyScoreRepository;
import da_ni_ni.backend.intimacy.repository.IntimacyTestResponseRepository;
import da_ni_ni.backend.user.domain.User;
import da_ni_ni.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class IntimacyServiceIntegrationTest {

    @Mock
    private IntimacyScoreRepository scoreRepository;

    @Mock
    private IntimacyTestResponseRepository responseRepository;

    @Mock
    private UserRepository userRepository;

    private IntimacyService intimacyService;

    private User testUser;
    private FamilyGroup testGroup;
    private List<AnswerDto> validAnswers;

    @BeforeEach
    void setUp() {
        intimacyService = new IntimacyService(scoreRepository, responseRepository, userRepository, null);

        // 빌더 패턴을 사용하여 사용자 생성
        testUser = User.builder()
                .id(1L)
                .name("테스트사용자")
                .build();

        // 빌더 패턴을 사용하여 그룹 생성
        testGroup = FamilyGroup.builder()
                .id(1L)
                .name("테스트가족")
                .build();

        testUser.setFamilyGroup(testGroup);

        // 유효한 답변 리스트 생성
        validAnswers = Arrays.asList(
                new AnswerDto(1, 5),
                new AnswerDto(2, 4),
                new AnswerDto(3, 3),
                new AnswerDto(4, 4),
                new AnswerDto(5, 5),
                new AnswerDto(6, 2),
                new AnswerDto(7, 3),
                new AnswerDto(8, 4),
                new AnswerDto(9, 5),
                new AnswerDto(10, 3)
        );
    }

    @Test
    @DisplayName("통합 테스트: 테스트 답변 제출 및 점수 계산")
    void submitAnswersAndCalculateScore() {
        // Given
        when(scoreRepository.save(any(IntimacyScore.class))).thenAnswer(invocation -> {
            IntimacyScore score = invocation.getArgument(0);
            score.setIntimacyId(1); // ID 설정
            return score;
        });

        // When
        intimacyService.submitAnswers(testUser, validAnswers);

        // Then
        verify(scoreRepository, times(1)).save(any(IntimacyScore.class));
        verify(responseRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("통합 테스트: 개인 점수 조회")
    void getPersonalScoreTest() {
        // Given
        IntimacyScore score = new IntimacyScore();
        score.setUser(testUser);
        score.setScore(76); // 테스트 점수
        score.setTestDate(LocalDate.now());

        when(scoreRepository.findFirstByUserOrderByTestDateDesc(testUser)).thenReturn(Optional.of(score));

        // When
        PersonalScoreResponse response = intimacyService.getPersonalScore(testUser);

        // Then
        assertEquals("나", response.getMemberName());
        assertEquals(76, response.getScore());
        verify(scoreRepository, times(1)).findFirstByUserOrderByTestDateDesc(testUser);
    }

    @Test
    @DisplayName("통합 테스트: 개인 점수 조회 실패 - 기록 없음")
    void getPersonalScoreTest_NoRecord() {
        // Given
        when(scoreRepository.findFirstByUserOrderByTestDateDesc(testUser)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IntimacyRecordNotFoundException.class, () -> {
            intimacyService.getPersonalScore(testUser);
        });
    }

    @Test
    @DisplayName("통합 테스트: 가족 점수 조회")
    void getFamilyScoreTest() {
        // Given
        User user1 = User.builder().id(1L).build();
        User user2 = User.builder().id(2L).build();

        IntimacyScore score1 = new IntimacyScore();
        score1.setScore(80);
        IntimacyScore score2 = new IntimacyScore();
        score2.setScore(90);

        List<User> familyMembers = Arrays.asList(user1, user2);

        when(userRepository.findAllByFamilyGroup(testGroup)).thenReturn(familyMembers);
        when(scoreRepository.findFirstByUserOrderByTestDateDesc(user1)).thenReturn(Optional.of(score1));
        when(scoreRepository.findFirstByUserOrderByTestDateDesc(user2)).thenReturn(Optional.of(score2));

        // When
        FamilyScoreResponse response = intimacyService.getFamilyScore(testUser);

        // Then
        assertEquals("테스트가족", response.getFamilyName());
        assertEquals(85.0, response.getAverageScore());
    }

    @Test
    @DisplayName("통합 테스트: 가족 점수 조회 실패 - 그룹 없음")
    void getFamilyScoreTest_NoGroup() {
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
}