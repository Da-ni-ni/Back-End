package da_ni_ni.backend.user.service;

import da_ni_ni.backend.user.domain.User;
import da_ni_ni.backend.user.repository.UserRepository;
import da_ni_ni.backend.qna.exception.BadRequestException;
import da_ni_ni.backend.qna.exception.ForbiddenException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(
        properties = {
                // JWT 설정은 쓰이지 않지만, DB 설정은 동일하게 맞춰줍니다
                "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
        }
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthServiceIntegrationTest {

    @Autowired
    private AuthService authService;  // 테스트 대상 :contentReference[oaicite:0]{index=0}:contentReference[oaicite:1]{index=1}

    @Autowired
    private UserRepository userRepo;

    private User userNoGroup;
    private User userInGroupA;
    private User userInGroupB;

    @BeforeEach
    void setUp() {
        // 그룹이 없는 유저
        userNoGroup = userRepo.save(User.builder()
                .name("noGroup")
                .email("no@group.com")
                .passwordHash("irrelevant")
                .groupId(null)
                .build());

        // 그룹 A 소속 유저 두 명
        userInGroupA = userRepo.save(User.builder()
                .name("memberA1")
                .email("a1@group.com")
                .passwordHash("pw")
                .groupId(100L)
                .build());
        userInGroupB = userRepo.save(User.builder()
                .name("memberA2")
                .email("a2@group.com")
                .passwordHash("pw")
                .groupId(100L)
                .build());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_withoutAuthentication_throwsBadRequest() {
        // 인증 정보가 없으면 BadRequestException
        SecurityContextHolder.clearContext();
        assertThrows(BadRequestException.class, () -> authService.getCurrentUser());
    }

    @Test
    void getCurrentUser_withAuthentication_returnsThatUser() {
        // SecurityContext에 넣을 인증 토큰을 생성
        var auth = new UsernamePasswordAuthenticationToken(
                userNoGroup.getEmail(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // 서비스 호출 시 DB에서 같은 이메일의 User를 찾아 반환
        User found = authService.getCurrentUser();
        assertThat(found.getEmail()).isEqualTo(userNoGroup.getEmail());
        assertThat(found.getName()).isEqualTo(userNoGroup.getName());
    }

    @Test
    void getApprovedUser_whenNoGroup_throwsForbidden() {
        // 그룹이 없는 상태로 getApprovedUser 호출 → ForbiddenException
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        userNoGroup.getEmail(), null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                )
        );
        assertThrows(ForbiddenException.class, () -> authService.getApprovedUser());
    }

    @Test
    void getApprovedUser_whenHasGroup_returnsUser() {
        // 그룹이 있는 userInGroupA로 인증
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        userInGroupA.getEmail(), null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                )
        );
        User approved = authService.getApprovedUser();
        assertThat(approved.getEmail()).isEqualTo(userInGroupA.getEmail());
        assertThat(approved.getGroupId()).isEqualTo(100L);
    }

    @Test
    void getFamilyMembers_returnsAllUsersInSameGroup() {
        // userInGroupA 로 인증 설정
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        userInGroupA.getEmail(), null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                )
        );
        // groupId = 100L 인 모든 멤버 조회
        var members = authService.getFamilyMembers();
        assertThat(members)
                .extracting(User::getEmail)
                .containsExactlyInAnyOrder("a1@group.com", "a2@group.com");
    }
}
