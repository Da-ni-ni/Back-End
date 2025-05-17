package da_ni_ni.backend.intimacy.exception;

import da_ni_ni.backend.qna.exception.BadRequestException;

/**
 * 사용자가 그룹에 속해 있지 않을 때 발생시키는 예외
 */
public class GroupNotFoundException extends BadRequestException {
    public GroupNotFoundException() {
        super("소속된 그룹이 없습니다.");
    }
}
