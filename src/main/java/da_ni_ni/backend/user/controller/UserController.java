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
        // 1) userDetails 체크
        if (userDetails == null) {
            log.warn("[UserController.updateFcmToken] 인증 정보 없음(토큰이 없거나 만료) → 401 반환");
            throw new RuntimeException("인증 정보가 없습니다");  // → 전역 핸들러로 던지면 스택트레이스가 남음
        }
        if (request == null || request.getFcmToken() == null || request.getFcmToken().isBlank()) {
            log.warn("[UserController.updateFcmToken] 잘못된 요청: request 또는 fcmToken 누락");
            throw new IllegalArgumentException("유효하지 않은 FCM 토큰");
        }

        log.info("[UserController.updateFcmToken] 호출됨 → 사용자={}, 요청토큰={}",
                userDetails.getUsername(), request.getFcmToken());

        try {
            userService.updateFcmToken(userDetails.getUsername(), request.getFcmToken());
        } catch (Exception e) {
            // 2) 서비스 레이어에서 발생하는 예외를 catch 하지 않고 전역 핸들러로 전달
            log.error("[UserController.updateFcmToken] 서비스 호출 중 예외: ", e);
            throw e;  // → GlobalExceptionHandler.handleException으로 전파
        }

        log.info("[UserController.updateFcmToken] FCM 토큰 업데이트 성공");
        return ResponseEntity.ok().build();
    }
}