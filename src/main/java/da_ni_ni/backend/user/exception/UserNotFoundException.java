package da_ni_ni.backend.user.exception;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException() {
        super("User를 찾을 수 없습니다.");
    }

    public UserNotFoundException(Long userId) {
        super("User: " + userId + " 를 찾을 수 없습니다.");
    }
}

