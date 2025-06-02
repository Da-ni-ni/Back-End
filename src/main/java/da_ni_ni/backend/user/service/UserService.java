package da_ni_ni.backend.user.service;

import da_ni_ni.backend.intimacy.repository.IntimacyScoreRepository;
import da_ni_ni.backend.user.domain.RefreshToken;
import da_ni_ni.backend.user.domain.User;
import da_ni_ni.backend.user.dto.*;
import da_ni_ni.backend.user.exception.DuplicateEmailException;
import da_ni_ni.backend.user.exception.LoginFailedException;
import da_ni_ni.backend.user.jwt.JwtTokenProvider;
import da_ni_ni.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final IntimacyScoreRepository intimacyScoreRepository;

    public void signup(SignupRequestDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("이미 존재하는 이메일입니다.");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .nickName(request.getName())
                .build();

        userRepository.save(user);
    }

    @Transactional
    public LoginResponseDto login(LoginRequestDto req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new LoginFailedException("잘못된 이메일 또는 비밀번호입니다."));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new LoginFailedException("잘못된 이메일 또는 비밀번호입니다.");
        }

        // Access Token과 Refresh Token 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getEmail());

        // 가족 그룹 ID 가져오기 (없으면 null)
        Long familyGroupId = null;
        if (user.getFamilyGroup() != null) {
            familyGroupId = user.getFamilyGroup().getId();
        }

        // 친밀도 검사 결과 존재 여부 확인 - 수정된 부분
        boolean hasIntimacyTest = intimacyScoreRepository.findFirstByUserOrderByTestDateDescCreatedAtDesc(user).isPresent();

        return LoginResponseDto.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .token(accessToken)
                .refreshToken(refreshToken.getToken())
                .familyGroupId(familyGroupId)
                .hasIntimacyTest(hasIntimacyTest)
                .build();
    }

    public boolean isEmailDuplicated(String email) {
        return userRepository.existsByEmail(email);
    }

    // 토큰 재발급 메서드
    @Transactional
    public TokenReissueResponseDto reissueToken(TokenReissueRequestDto request) {
        String requestRefreshToken = request.getRefreshToken();

//        // 테스트용 만료된 토큰 특수 처리
//        if ("expired-refresh-token".equals(requestRefreshToken)) {
//            throw new ExpiredRefreshTokenException("Refresh token이 만료되었습니다. 다시 로그인해주세요.");
//        }

        // 기존 코드
        RefreshToken refreshToken = refreshTokenService.findByTokenOrThrow(requestRefreshToken);
        refreshTokenService.verifyExpiration(refreshToken);

        // 새로운 토큰들 생성
        String userEmail = refreshToken.getUserEmail();
        String newAccessToken = jwtTokenProvider.createAccessToken(userEmail);
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(userEmail);

        return new TokenReissueResponseDto(newAccessToken, newRefreshToken.getToken());
    }


    // 로그아웃 시 refresh token 삭제
    @Transactional
    public void logout(String userEmail) {
        refreshTokenService.deleteByUserEmail(userEmail);
    }

    /**
     * 사용자의 FCM 토큰 업데이트
     */
    @Transactional
    public void updateFcmToken(String email, String fcmToken) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));

        user.updateFcmToken(fcmToken);
        userRepository.save(user);
    }

}