package da_ni_ni.backend.group.exception;

// 초대 코드가 존재하지 않을 때
public class InviteCodeNotFoundException extends RuntimeException {
    public InviteCodeNotFoundException() {
        super("초대 코드를 찾을 수 없습니다.");
    }
}
