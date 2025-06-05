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

        // 강제 로그 추가: 메서드 진입 여부
        log.info("[UserController.updateFcmToken] 진입 → userDetails={}, request={}",
                userDetails, request);

        // 1) userDetails 체크
        if (userDetails == null) {
            log.warn("[UserController.updateFcmToken] 인증 정보가 없습니다 (userDetails == null)");
            throw new RuntimeException("인증 정보가 없습니다");
        }

        // 2) request.getFcmToken() 체크
        if (request == null) {
            log.warn("[UserController.updateFcmToken] Request 바인딩이 안 됨 (request == null)");
            throw new RuntimeException("요청 바디가 비어 있습니다");
        }
        if (request.getFcmToken() == null) {
            log.warn("[UserController.updateFcmToken] fcmToken이 null입니다 → request={}", request);
            throw new RuntimeException("유효하지 않은 FCM 토큰: null");
        }
        if (request.getFcmToken().isBlank()) {
            log.warn("[UserController.updateFcmToken] fcmToken이 빈 문자열입니다 → request={}", request);
            throw new RuntimeException("유효하지 않은 FCM 토큰: 빈 문자열");
        }

        log.info("[UserController.updateFcmToken] userDetails.getUsername()={}, request.getFcmToken()={}",
                userDetails.getUsername(), request.getFcmToken());

        try {
            userService.updateFcmToken(userDetails.getUsername(), request.getFcmToken());
        } catch (Exception e) {
            log.error("[UserController.updateFcmToken] userService.updateFcmToken 중 예외 발생", e);
            throw e;  // 전역 핸들러로 전파
        }

        log.info("[UserController.updateFcmToken] FCM 토큰 업데이트 정상 종료");
        return ResponseEntity.ok().build();
    }
}