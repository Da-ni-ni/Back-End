package da_ni_ni.backend.group.exception;

// 이미 그룹에 가입한 유저가 또 가입하려 할 때
public class AlreadyInGroupException extends RuntimeException {
    public AlreadyInGroupException() {
        super("이미 그룹에 가입되어 있습니다.");
    }
}

