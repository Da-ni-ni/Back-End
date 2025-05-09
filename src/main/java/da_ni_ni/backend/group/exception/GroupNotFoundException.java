package da_ni_ni.backend.group.exception;

// 그룹이 존재하지 않을 때
public class GroupNotFoundException extends RuntimeException {
    public GroupNotFoundException() {
        super("그룹을 찾을 수 없습니다.");
    }
}

