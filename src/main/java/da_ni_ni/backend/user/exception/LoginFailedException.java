package da_ni_ni.backend.user.exception;

// 로그인 실패 (이메일 없음 or 비밀번호 불일치)
public class LoginFailedException extends RuntimeException {
    public LoginFailedException(String message) {
        super(message);
    }
}