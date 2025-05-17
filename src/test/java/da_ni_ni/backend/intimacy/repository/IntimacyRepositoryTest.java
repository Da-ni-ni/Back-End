package da_ni_ni.backend.intimacy.repository;

import da_ni_ni.backend.intimacy.domain.IntimacyScore;
import da_ni_ni.backend.intimacy.domain.IntimacyTestResponse;
import da_ni_ni.backend.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.format_sql=true",
        "spring.jpa.properties.hibernate.show_sql=true",
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
public class IntimacyRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private IntimacyScoreRepository scoreRepository;

    @Autowired
    private IntimacyTestResponseRepository responseRepository;

    /**
     * 테스트용 User 생성 헬퍼 메서드
     */
    private User createTestUser(String email) {
        User user = User.builder()
                .name("테스트사용자")
                .email(email)
                .passwordHash("password")
                .nickName("닉네임")
                // familyGroup과 emotion은 null로 둡니다.
                .build();
        // 저장 및 즉시 반영
        User saved = entityManager.persist(user);
        entityManager.flush();
        return saved;
    }

    @Test
    @DisplayName("IntimacyScore 저장 및 조회 테스트")
    void saveAndFindIntimacyScore() {
        // Given
        User user = createTestUser("user1@example.com");

        IntimacyScore score = new IntimacyScore();
        score.setUser(user);
        score.setScore(85);
        score.setTestDate(LocalDate.now());
        score.setCreatedAt(LocalDateTime.now());

        // When
        IntimacyScore savedScore = scoreRepository.save(score);

        // Then
        assertNotNull(savedScore.getIntimacyId());
        assertEquals(85, savedScore.getScore());
        assertEquals(user.getId(), savedScore.getUser().getId());
    }

    @Test
    @DisplayName("IntimacyTestResponse 저장 및 조회 테스트")
    void saveAndFindIntimacyTestResponse() {
        // Given
        User user = createTestUser("user2@example.com");

        // 먼저 IntimacyScore 저장
        IntimacyScore score = new IntimacyScore();
        score.setUser(user);
        score.setScore(85);
        score.setTestDate(LocalDate.now());
        score.setCreatedAt(LocalDateTime.now());
        IntimacyScore savedScore = entityManager.persist(score);
        entityManager.flush();

        // 그 다음 TestResponse 저장
        IntimacyTestResponse response = new IntimacyTestResponse();
        response.setIntimacyScore(savedScore);
        response.setAnswer1((byte)5);
        response.setAnswer2((byte)4);
        response.setAnswer3((byte)3);
        response.setAnswer4((byte)4);
        response.setAnswer5((byte)5);
        response.setAnswer6((byte)2);
        response.setAnswer7((byte)3);
        response.setAnswer8((byte)4);
        response.setAnswer9((byte)5);
        response.setAnswer10((byte)3);
        response.setCreatedAt(LocalDateTime.now());

        // When
        IntimacyTestResponse savedResponse = responseRepository.save(response);

        // Then
        // IntimacyTestResponse가 IntimacyScore와 동일한 ID를 공유하도록 매핑되어 있다면
        assertEquals(savedScore.getIntimacyId(), savedResponse.getIntimacyId());
        assertEquals((byte)5, savedResponse.getAnswer1());
        assertEquals((byte)4, savedResponse.getAnswer2());
    }

    @Test
    @DisplayName("사용자별 최신 IntimacyScore 조회 테스트")
    void findFirstByUserOrderByTestDateDesc() {
        // Given
        User user = createTestUser("user3@example.com");

        // 오래된 점수
        IntimacyScore oldScore = new IntimacyScore();
        oldScore.setUser(user);
        oldScore.setScore(70);
        oldScore.setTestDate(LocalDate.now().minusDays(5));
        oldScore.setCreatedAt(LocalDateTime.now().minusDays(5));
        entityManager.persist(oldScore);

        // 최신 점수
        IntimacyScore newScore = new IntimacyScore();
        newScore.setUser(user);
        newScore.setScore(85);
        newScore.setTestDate(LocalDate.now());
        newScore.setCreatedAt(LocalDateTime.now());
        entityManager.persist(newScore);

        entityManager.flush();

        // When
        Optional<IntimacyScore> latestScore = scoreRepository.findFirstByUserOrderByTestDateDesc(user);

        // Then
        assertTrue(latestScore.isPresent());
        assertEquals(85, latestScore.get().getScore());
        assertEquals(LocalDate.now(), latestScore.get().getTestDate());
    }

    @Test
    @DisplayName("점수가 없는 사용자의 IntimacyScore 조회 테스트")
    void findFirstByUserOrderByTestDateDesc_UserNotFound() {
        // Given: DB에는 user만 있고, score는 없음
        User userWithoutScore = createTestUser("user4@example.com");

        // When
        Optional<IntimacyScore> result = scoreRepository.findFirstByUserOrderByTestDateDesc(userWithoutScore);

        // Then
        assertFalse(result.isPresent());
    }
}
