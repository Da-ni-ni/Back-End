package da_ni_ni.backend.daily.exception;

import da_ni_ni.backend.user.domain.User;

public class CommentNotFoundException extends RuntimeException{

    public CommentNotFoundException() {
        super("댓글을 찾을 수 없습니다.");
    }

    public CommentNotFoundException(Long dailyId) {
        super(" Daily: " + dailyId + "의 댓글을 찾을 수 없습니다.");
    }

    public CommentNotFoundException(User user) {
        super("Member: " + user.getNickName() + "의 댓글을 찾을 수 없습니다.");
    }
}
