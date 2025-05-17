package da_ni_ni.backend.intimacy.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class IntimacyDtoTest {

    @Test
    @DisplayName("AnswerDto 생성 및 Getter/Setter 검증")
    void answerDtoTest() {
        // Given & When
        AnswerDto dto = new AnswerDto(1, 4);
        
        // Then
        assertEquals(1, dto.getQuestionId());
        assertEquals(4, dto.getAnswer());
        
        // When - setter 테스트
        dto.setQuestionId(2);
        dto.setAnswer(5);
        
        // Then
        assertEquals(2, dto.getQuestionId());
        assertEquals(5, dto.getAnswer());
    }
    
    @Test
    @DisplayName("FamilyScoreResponse 생성 및 Getter 검증")
    void familyScoreResponseTest() {
        // Given & When
        FamilyScoreResponse response = new FamilyScoreResponse("행복가족", 85.5);
        
        // Then
        assertEquals("행복가족", response.getFamilyName());
        assertEquals(85.5, response.getAverageScore());
    }
    
    @Test
    @DisplayName("PersonalScoreResponse 생성 및 Getter 검증")
    void personalScoreResponseTest() {
        // Given & When
        PersonalScoreResponse response = new PersonalScoreResponse("나", 90);
        
        // Then
        assertEquals("나", response.getMemberName());
        assertEquals(90, response.getScore());
    }
    
    @Test
    @DisplayName("TestAnswerRequest 생성 및 Getter/Setter 검증")
    void testAnswerRequestTest() {
        // Given
        TestAnswerRequest request = new TestAnswerRequest();
        List<AnswerDto> answers = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            answers.add(new AnswerDto(i, i % 5 + 1));
        }
        
        // When
        request.setAnswers(answers);
        
        // Then
        assertEquals(10, request.getAnswers().size());
        assertEquals(1, request.getAnswers().get(0).getQuestionId());
        assertEquals(2, request.getAnswers().get(0).getAnswer());
        assertEquals(10, request.getAnswers().get(9).getQuestionId());
        assertEquals(1, request.getAnswers().get(9).getAnswer());
    }
    
    @Test
    @DisplayName("TestAnswerRequest 기본 생성자 검증")
    void testAnswerRequestDefaultConstructorTest() {
        // Given & When
        TestAnswerRequest request = new TestAnswerRequest();
        
        // Then
        assertNull(request.getAnswers());
    }
}