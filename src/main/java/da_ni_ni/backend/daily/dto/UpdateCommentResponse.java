package da_ni_ni.backend.daily.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import da_ni_ni.backend.daily.domain.Comment;
import da_ni_ni.backend.common.ResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;


@Getter
@Builder
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UpdateCommentResponse implements ResponseDto {
    private Long commentId;
    private LocalDateTime updatedAt;

    public static UpdateCommentResponse createWith(Comment comment) {
        return UpdateCommentResponse.builder()
                .commentId(comment.getCommentId())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}