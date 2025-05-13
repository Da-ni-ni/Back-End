package da_ni_ni.backend.daily.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import da_ni_ni.backend.daily.domain.Comment;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CommentDetailData {
    private Long commentId;
    private String authorName;
    private String content;
    private LocalDateTime createdAt;

    public static CommentDetailData from(Comment comment) {
        return new CommentDetailData(
                comment.getCommentId(),
                comment.getUser().getNickName() != null ? comment.getUser().getNickName() : comment.getUser().getName(),
                comment.getContent() != null ? comment.getContent() : "내용 없음",
                comment.getCreatedAt()
        );
    }
}
