package da_ni_ni.backend.user.service;

import da_ni_ni.backend.qna.exception.BadRequestException;
import da_ni_ni.backend.qna.exception.ForbiddenException;
import da_ni_ni.backend.user.domain.User;
import da_ni_ni.backend.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.doReturn;
import static org.mockito.BDDMockito.given;

class AuthServiceTest {

    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Test
    void getCurrentUser_withNoAuthentication_shouldThrowBadRequest() {
        // SecurityContextHolder 에 인증 정보가 아예 없을 때
        SecurityContextHolder.clearContext();
        assertThatThrownBy(() -> authService.getCurrentUser())
                .isInstanceOf(BadRequestException.class)
                .hasMessage("로그인 사용자 정보를 찾을 수 없습니다.");
    }

    @Test
    void getCurrentUser_withAnonymousAuthentication_shouldThrowBadRequest() {
        // 익명 토큰에 최소 하나의 권한을 넣어야 한다.
        AnonymousAuthenticationToken anon =
                new AnonymousAuthenticationToken(
                        "anonymousKey",
                        "anonymousUser",
                        List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
                );
        SecurityContextHolder.getContext().setAuthentication(anon);

        assertThatThrownBy(() -> authService.getCurrentUser())
                .isInstanceOf(BadRequestException.class)
                .hasMessage("로그인 사용자 정보를 찾을 수 없습니다.");
    }

    @Test
    void getCurrentUser_whenUserNotFoundInRepo_shouldThrowBadRequest() {
        // 인증 정보는 있지만, repository 에 없는 이메일인 경우
        String email = "noone@example.com";
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        given(userRepo.findByEmail(email)).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getCurrentUser())
                .isInstanceOf(BadRequestException.class)
                .hasMessage("로그인 사용자 정보를 찾을 수 없습니다.");
    }

    @Test
    void getCurrentUser_withValidAuthentication_shouldReturnUser() {
        String email = "user@example.com";
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        User user = User.builder()
                .userId(1L)
                .email(email)
                .name("홍길동")
                .groupId(null)
                .build();
        given(userRepo.findByEmail(email)).willReturn(Optional.of(user));

        User result = authService.getCurrentUser();
        assertThat(result).isSameAs(user);
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Test
    void getApprovedUser_whenNoGroup_shouldThrowForbidden() {
        // getCurrentUser()는 이미 위에서 검증됐다고 가정하고, 여기서는 groupId == null
        User noGroupUser = User.builder()
                .userId(2L)
                .email("a@b.com")
                .name("테스트")
                .groupId(null)
                .build();
        // stub getCurrentUser()
        try (MockedStatic<SecurityContextHolder> ignored = Mockito.mockStatic(SecurityContextHolder.class)) {
            // 이 블록 안에서는 authService.getCurrentUser() 호출 시 noGroupUser 리턴
            // 그러나 간단히는 spy(authService) 해서 getCurrentUser()만 오버라이드해도 됩니다.
        }

        // 대신 Spy 를 사용해보겠습니다
        AuthService spySvc = Mockito.spy(authService);
        doReturn(noGroupUser).when(spySvc).getCurrentUser();

        assertThatThrownBy(spySvc::getApprovedUser)
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("가입 승인된 그룹이 없습니다.");
    }

    @Test
    void getApprovedUser_withGroup_shouldReturnUser() {
        User inGroup = User.builder()
                .userId(3L)
                .email("b@b.com")
                .name("테스트2")
                .groupId(42L)
                .build();

        AuthService spySvc = Mockito.spy(authService);
        doReturn(inGroup).when(spySvc).getCurrentUser();

        User result = spySvc.getApprovedUser();
        assertThat(result).isSameAs(inGroup);
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Test
    void getFamilyMembers_shouldReturnListFromRepo() {
        long gid = 99L;
        User inGroup = User.builder()
                .userId(4L)
                .email("c@c.com")
                .name("테스트3")
                .groupId(gid)
                .build();

        // 스파이로 getApprovedUser()가 inGroup 리턴하도록
        AuthService spySvc = Mockito.spy(authService);
        doReturn(inGroup).when(spySvc).getApprovedUser();

        List<User> members = List.of(
                User.builder().userId(5L).email("x@x.com").name("멤버1").groupId(gid).build(),
                User.builder().userId(6L).email("y@y.com").name("멤버2").groupId(gid).build()
        );
        given(userRepo.findAllByGroupId(gid)).willReturn(members);

        List<User> result = spySvc.getFamilyMembers();
        assertThat(result).containsExactlyElementsOf(members);
    }
}
