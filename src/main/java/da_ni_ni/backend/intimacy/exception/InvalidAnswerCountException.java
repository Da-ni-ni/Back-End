package da_ni_ni.backend.intimacy.exception;

import da_ni_ni.backend.qna.exception.BadRequestException;

/**
 * 테스트 답변 개수가 10개가 아닐 때 발생시키는 예외
 */
public class InvalidAnswerCountException extends BadRequestException {
    public InvalidAnswerCountException() {
        super("테스트 답변은 정확히 10개여야 합니다.");
    }
}