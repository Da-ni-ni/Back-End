package da_ni_ni.backend.intimacy.exception;

import da_ni_ni.backend.qna.exception.BadRequestException;

/**
 * 친밀도 점수 기록이 없을 때 발생시키는 예외
 */
public class IntimacyRecordNotFoundException extends BadRequestException {
    public IntimacyRecordNotFoundException() {
        super("친밀도 점수 기록을 찾을 수 없습니다.");
    }
}