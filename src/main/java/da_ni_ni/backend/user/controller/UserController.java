package da_ni_ni.backend.user.controller;

import da_ni_ni.backend.user.dto.*;
import da_ni_ni.backend.user.service.AuthService;
import da_ni_ni.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final AuthService authService;

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

    // 토큰 재발급
    @PostMapping("/reissue")
    public ResponseEntity<TokenReissueResponseDto> reissueToken(@RequestBody TokenReissueRequestDto request) {
        TokenReissueResponseDto response = userService.reissueToken(request);
        return ResponseEntity.ok(response);
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        String userEmail = authService.getCurrentUser().getEmail();
        userService.logout(userEmail);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/fcm-token")
    public ResponseEntity<Void> updateFcmToken(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody FcmTokenUpdateRequest request) {
        // 여기서 “요청 진입” 로그
        log.info("[UserController.updateFcmToken] 호출됨 → userEmail={}, 요청 FCM 토큰={}",
                userDetails.getUsername(), request.getFcmToken());

        userService.updateFcmToken(userDetails.getUsername(), request.getFcmToken());

        // “업데이트 완료” 로그
        log.info("[UserController.updateFcmToken] FCM 토큰 업데이트 성공 → userEmail={}", userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}