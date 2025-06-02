package da_ni_ni.backend.intimacy.controller;

import da_ni_ni.backend.intimacy.dto.AnswerDto;
import da_ni_ni.backend.intimacy.dto.FamilyScoreResponse;
import da_ni_ni.backend.intimacy.dto.PersonalScoreResponse;
import da_ni_ni.backend.intimacy.dto.TestAnswerRequest;
import da_ni_ni.backend.intimacy.service.IntimacyService;
import da_ni_ni.backend.user.domain.User;
import da_ni_ni.backend.user.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IntimacyControllerTest {

    @Mock
    private IntimacyService intimacyService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private IntimacyController intimacyController;

    private User testUser;
    private TestAnswerRequest testRequest;
    private PersonalScoreResponse personalScoreResponse;
    private FamilyScoreResponse familyScoreResponse;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 설정
        testUser = User.builder()
                .id(1L)
                .name("테스트사용자")
                .build();

        // 테스트용 요청 객체 생성
        List<AnswerDto> answers = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            answers.add(new AnswerDto(i, i % 5 + 1));
        }
        testRequest = new TestAnswerRequest();
        testRequest.setAnswers(answers);

        // 테스트용 응답 객체 생성
        personalScoreResponse = new PersonalScoreResponse("나", 75);
        familyScoreResponse = new FamilyScoreResponse("테스트가족", 80.5);
    }

    @Test
    @DisplayName("답변 제출 성공")
    void submitAnswers_Success() {
        // Given
        when(authService.getCurrentUser()).thenReturn(testUser);
        doNothing().when(intimacyService).submitAnswers(eq(testUser), anyList());

        // When
        ResponseEntity<Void> response = intimacyController.submitAnswers(testRequest);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(authService, times(1)).getCurrentUser();
        verify(intimacyService, times(1)).submitAnswers(eq(testUser), eq(testRequest.getAnswers()));
    }

    @Test
    @DisplayName("개인 점수 조회 성공")
    void getPersonal_Success() {
        // Given
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(intimacyService.getPersonalScore(testUser)).thenReturn(personalScoreResponse);

        // When
        ResponseEntity<PersonalScoreResponse> response = intimacyController.getPersonal();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("나", response.getBody().getMemberName());
        assertEquals(75, response.getBody().getScore());
        verify(authService, times(1)).getCurrentUser();
        verify(intimacyService, times(1)).getPersonalScore(testUser);
    }

    @Test
    @DisplayName("가족 점수 조회 성공")
    void getFamily_Success() {
        // Given
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(intimacyService.getFamilyScore(testUser)).thenReturn(familyScoreResponse);

        // When
        ResponseEntity<FamilyScoreResponse> response = intimacyController.getFamily();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("테스트가족", response.getBody().getFamilyName());
        assertEquals(80.5, response.getBody().getAverageScore());
        verify(authService, times(1)).getCurrentUser();
        verify(intimacyService, times(1)).getFamilyScore(testUser);
    }
}