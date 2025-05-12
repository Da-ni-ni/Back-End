package da_ni_ni.backend.group.exception;

// 초대 요청을 찾을 수 없을 때
public class InvitationRequestNotFoundException extends RuntimeException {
    public InvitationRequestNotFoundException() {
        super("초대 요청이 존재하지 않습니다.");
    }
}

