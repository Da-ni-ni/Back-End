package da_ni_ni.backend.user.repository;

import da_ni_ni.backend.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
// Hibernate DDL 방언을 H2 전용으로 바꿔버립니다.
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})

class UserRepositoryTest {

    @Autowired
    private UserRepository userRepo;

    @Test
    @DisplayName("이메일로 회원 조회 & existsByEmail 검증")
    void findByEmail_and_existsByEmail() {
        // given: nickname 과 같은 NOT NULL 컬럼도 세팅
        User saved = userRepo.save(
                User.builder()
                        .name("repo")
                        .email("r@e.com")
                        .passwordHash("h")
                        .nickname("nick")
                        .build()
        );

        // when
        Optional<User> found = userRepo.findByEmail("r@e.com");
        boolean existsTrue  = userRepo.existsByEmail("r@e.com");
        boolean existsFalse = userRepo.existsByEmail("nope@e.com");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(saved.getUserId());
        assertThat(existsTrue).isTrue();
        assertThat(existsFalse).isFalse();
    }
}
