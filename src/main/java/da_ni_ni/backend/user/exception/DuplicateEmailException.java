
package da_ni_ni.backend.user.exception;

// 이메일 중복
public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String message) {
        super(message);
    }
}
