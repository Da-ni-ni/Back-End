package da_ni_ni.backend.user.controller;

import da_ni_ni.backend.user.dto.EmailCheckRequestDto;
import da_ni_ni.backend.user.dto.EmailCheckResponseDto;
import da_ni_ni.backend.user.dto.LoginRequestDto;
import da_ni_ni.backend.user.dto.LoginResponseDto;
import da_ni_ni.backend.user.dto.SignupRequestDto;
import da_ni_ni.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@RequestBody SignupRequestDto request) {
        userService.signup(request);
        return ResponseEntity.ok().build();
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto req) {
        LoginResponseDto dto = userService.login(req);
        return ResponseEntity.ok(dto);
    }

    // 이메일 중복 확인
    @PostMapping("/check-email")
    public ResponseEntity<EmailCheckResponseDto> checkEmail(@RequestBody EmailCheckRequestDto request) {
        boolean exists = userService.isEmailDuplicated(request.getEmail());
        return ResponseEntity.ok(new EmailCheckResponseDto(exists));
    }

    // 예시) 현재 사용자 조회
    /*
    @GetMapping("/me")
    public UserResponseDto getCurrentUser(@AuthenticationPrincipal CustomUserDetails principal) {
        return new UserResponseDto(
                principal.getName(),
                principal.getEmail(),
                principal.getGroupId()
    }
     */
}