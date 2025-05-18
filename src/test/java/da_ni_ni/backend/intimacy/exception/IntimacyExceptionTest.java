package da_ni_ni.backend.intimacy.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class IntimacyExceptionTest {

    @Test
    @DisplayName("GroupNotFoundException 예외 메시지 검증")
    void groupNotFoundException() {
        // Given & When
        GroupNotFoundException exception = new GroupNotFoundException();
        
        // Then
        assertEquals("소속된 그룹이 없습니다.", exception.getMessage());
    }
    
    @Test
    @DisplayName("IntimacyRecordNotFoundException 예외 메시지 검증")
    void intimacyRecordNotFoundException() {
        // Given & When
        IntimacyRecordNotFoundException exception = new IntimacyRecordNotFoundException();
        
        // Then
        assertEquals("친밀도 점수 기록을 찾을 수 없습니다.", exception.getMessage());
    }
    
    @Test
    @DisplayName("InvalidAnswerCountException 예외 메시지 검증")
    void invalidAnswerCountException() {
        // Given & When
        InvalidAnswerCountException exception = new InvalidAnswerCountException();
        
        // Then
        assertEquals("테스트 답변은 정확히 10개여야 합니다.", exception.getMessage());
    }
}