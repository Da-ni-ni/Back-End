package da_ni_ni.backend.user.service;

import da_ni_ni.backend.user.jwt.JwtTokenProvider;
import da_ni_ni.backend.user.domain.User;
import da_ni_ni.backend.user.dto.LoginRequestDto;
import da_ni_ni.backend.user.dto.LoginResponseDto;
import da_ni_ni.backend.user.dto.SignupRequestDto;
import da_ni_ni.backend.user.exception.DuplicateEmailException;
import da_ni_ni.backend.user.exception.LoginFailedException;
import da_ni_ni.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

class UserServiceTest {

    private UserService userService;
    private UserRepository userRepo;
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        userRepo = mock(UserRepository.class);
        jwtTokenProvider = mock(JwtTokenProvider.class);
        userService = new UserService(userRepo, jwtTokenProvider, encoder);
    }

    @Test
    void signupSuccess() {
        // given
        SignupRequestDto dto = new SignupRequestDto();
        dto.setName("테스트");
        dto.setEmail("a@b.com");
        dto.setPassword("pass");
        given(userRepo.existsByEmail("a@b.com")).willReturn(false);

        // when
        userService.signup(dto);

        // then
        then(userRepo).should().save(argThat(user ->
                user.getName().equals("테스트") &&
                        encoder.matches("pass", user.getPasswordHash())
        ));
    }

    @Test
    void signupFailWhenEmailDuplicated() {
        // given
        SignupRequestDto dto = new SignupRequestDto();
        dto.setEmail("dup@b.com");
        given(userRepo.existsByEmail("dup@b.com")).willReturn(true);

        // when / then
        assertThatThrownBy(() -> userService.signup(dto))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessage("이미 존재하는 이메일입니다.");
    }

    @Test
    void loginSuccess() {
        // given
        LoginRequestDto req = new LoginRequestDto();
        req.setEmail("a@b.com");
        req.setPassword("pass");

        User user = User.builder()
                .name("테스트")
                .email("a@b.com")
                .passwordHash(encoder.encode("pass"))
                .groupId(123L)
                .build();

        given(userRepo.findByEmail("a@b.com")).willReturn(Optional.of(user));
        given(jwtTokenProvider.createToken("a@b.com")).willReturn("token-xyz");

        // when
        LoginResponseDto resp = userService.login(req);

        // then
        assertThat(resp.getName()).isEqualTo("테스트");
        assertThat(resp.getEmail()).isEqualTo("a@b.com");
        assertThat(resp.getToken()).isEqualTo("token-xyz");
        assertThat(resp.getGroupId()).isEqualTo(123L);
    }

    @Test
    void loginFailWhenEmailNotFound() {
        // given
        LoginRequestDto req = new LoginRequestDto();
        req.setEmail("nope@b.com");
        req.setPassword("pass");
        given(userRepo.findByEmail("nope@b.com")).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> userService.login(req))
                .isInstanceOf(LoginFailedException.class)
                .hasMessage("잘못된 이메일 또는 비밀번호입니다.");
    }

    @Test
    void loginFailWhenPasswordMismatch() {
        // given
        LoginRequestDto req = new LoginRequestDto();
        req.setEmail("a@b.com");
        req.setPassword("wrong");

        User user = User.builder()
                .email("a@b.com")
                .name("테스트")
                .passwordHash(encoder.encode("pass"))
                .build();
        given(userRepo.findByEmail("a@b.com")).willReturn(Optional.of(user));

        // when / then
        assertThatThrownBy(() -> userService.login(req))
                .isInstanceOf(LoginFailedException.class)
                .hasMessage("잘못된 이메일 또는 비밀번호입니다.");
    }
}
