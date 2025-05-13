package da_ni_ni.backend.user.service;

import da_ni_ni.backend.user.domain.User;
import da_ni_ni.backend.user.dto.LoginRequestDto;
import da_ni_ni.backend.user.dto.LoginResponseDto;
import da_ni_ni.backend.user.dto.SignupRequestDto;
import da_ni_ni.backend.user.exception.DuplicateEmailException;
import da_ni_ni.backend.user.exception.LoginFailedException;
import da_ni_ni.backend.user.repository.UserRepository;
import da_ni_ni.backend.user.jwt.JwtTokenProvider;
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

    public void signup(SignupRequestDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("이미 존재하는 이메일입니다.");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public LoginResponseDto login(LoginRequestDto req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new LoginFailedException("잘못된 이메일 또는 비밀번호입니다."));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new LoginFailedException("잘못된 이메일 또는 비밀번호입니다.");
        }

        String token = jwtTokenProvider.createToken(user.getEmail());

        return new LoginResponseDto(
                user.getName(),
                user.getEmail(),
                token
        );
    }

    public boolean isEmailDuplicated(String email) {
        return userRepository.existsByEmail(email);
    }
}