package da_ni_ni.backend.group.exception;

// 그룹 생성자가 아닌데 생성자 전용 작업(예: 초대 수락, 가족명 수정)을 시도할 때
public class NotGroupAdminUserException extends RuntimeException {
    public NotGroupAdminUserException() {
        super("그룹 생성자만 가능한 작업입니다.");
    }
}

