package da_ni_ni.backend.group.exception;

// 이미 요청을 보냈을 때
public class AlreadyRequestedJoinException extends RuntimeException {
    public AlreadyRequestedJoinException() {
        super("이미 초대요청을 보냈습니다.");
    }
}
