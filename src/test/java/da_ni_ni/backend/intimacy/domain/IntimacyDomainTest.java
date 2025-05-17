package da_ni_ni.backend.intimacy.domain;

import da_ni_ni.backend.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class IntimacyDomainTest {

    @Test
    @DisplayName("IntimacyScore 엔티티 생성 및 프로퍼티 검증")
    void createIntimacyScore() {
        // Given
        User user = User.builder()
                .id(1L)
                .name("테스트사용자")
                .build();

        LocalDate testDate = LocalDate.now();
        LocalDateTime createdAt = LocalDateTime.now();

        // When
        IntimacyScore score = IntimacyScore.builder()
                .intimacyId(1)
                .user(user)
                .score(80)
                .testDate(testDate)
                .createdAt(createdAt)
                .build();

        // Then
        assertEquals(1, score.getIntimacyId());
        assertEquals(user, score.getUser());
        assertEquals(80, score.getScore());
        assertEquals(testDate, score.getTestDate());
        assertEquals(createdAt, score.getCreatedAt());
    }

    
    @Test
    @DisplayName("IntimacyTestResponse 엔티티 생성 및 프로퍼티 검증")
    void createIntimacyTestResponse() {
        // Given
        IntimacyScore score = new IntimacyScore();
        score.setIntimacyId(1);
        
        LocalDateTime createdAt = LocalDateTime.now();
        
        // When
        IntimacyTestResponse response = IntimacyTestResponse.builder()
            .intimacyId(1)
            .intimacyScore(score)
            .answer1((byte)5)
            .answer2((byte)4)
            .answer3((byte)3)
            .answer4((byte)4)
            .answer5((byte)5)
            .answer6((byte)2)
            .answer7((byte)3)
            .answer8((byte)4)
            .answer9((byte)5)
            .answer10((byte)3)
            .createdAt(createdAt)
            .build();
            
        // Then
        assertEquals(1, response.getIntimacyId());
        assertEquals(score, response.getIntimacyScore());
        assertEquals((byte)5, response.getAnswer1());
        assertEquals((byte)4, response.getAnswer2());
        assertEquals((byte)3, response.getAnswer3());
        assertEquals((byte)4, response.getAnswer4());
        assertEquals((byte)5, response.getAnswer5());
        assertEquals((byte)2, response.getAnswer6());
        assertEquals((byte)3, response.getAnswer7());
        assertEquals((byte)4, response.getAnswer8());
        assertEquals((byte)5, response.getAnswer9());
        assertEquals((byte)3, response.getAnswer10());
        assertEquals(createdAt, response.getCreatedAt());
    }

    @Test
    @DisplayName("IntimacyScore setter 검증")
    void intimacyScoreSetterTest() {
        // Given
        IntimacyScore score = new IntimacyScore();
        User user = User.builder()
                .id(2L)
                .build();
        LocalDate testDate = LocalDate.now();
        LocalDateTime createdAt = LocalDateTime.now();

        // When
        score.setIntimacyId(2);
        score.setUser(user);
        score.setScore(95);
        score.setTestDate(testDate);
        score.setCreatedAt(createdAt);

        // Then
        assertEquals(2, score.getIntimacyId());
        assertEquals(user, score.getUser());
        assertEquals(95, score.getScore());
        assertEquals(testDate, score.getTestDate());
        assertEquals(createdAt, score.getCreatedAt());
    }
    
    @Test
    @DisplayName("IntimacyTestResponse setter 검증")
    void intimacyTestResponseSetterTest() {
        // Given
        IntimacyTestResponse response = new IntimacyTestResponse();
        IntimacyScore score = new IntimacyScore();
        score.setIntimacyId(2);
        LocalDateTime createdAt = LocalDateTime.now();
        
        // When
        response.setIntimacyId(2);
        response.setIntimacyScore(score);
        response.setAnswer1((byte)1);
        response.setAnswer2((byte)2);
        response.setAnswer3((byte)3);
        response.setAnswer4((byte)4);
        response.setAnswer5((byte)5);
        response.setAnswer6((byte)4);
        response.setAnswer7((byte)3);
        response.setAnswer8((byte)2);
        response.setAnswer9((byte)1);
        response.setAnswer10((byte)3);
        response.setCreatedAt(createdAt);
        
        // Then
        assertEquals(2, response.getIntimacyId());
        assertEquals(score, response.getIntimacyScore());
        assertEquals((byte)1, response.getAnswer1());
        assertEquals((byte)2, response.getAnswer2());
        assertEquals((byte)3, response.getAnswer3());
        assertEquals((byte)4, response.getAnswer4());
        assertEquals((byte)5, response.getAnswer5());
        assertEquals((byte)4, response.getAnswer6());
        assertEquals((byte)3, response.getAnswer7());
        assertEquals((byte)2, response.getAnswer8());
        assertEquals((byte)1, response.getAnswer9());
        assertEquals((byte)3, response.getAnswer10());
        assertEquals(createdAt, response.getCreatedAt());
    }
}