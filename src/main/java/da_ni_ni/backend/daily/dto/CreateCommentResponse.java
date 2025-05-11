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
public class CreateCommentResponse implements ResponseDto {
    private Long commentId;
    private LocalDateTime createdAt;

    public static CreateCommentResponse createWith(Comment comment) {
        return CreateCommentResponse.builder()
                .commentId(comment.getCommentId())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}

