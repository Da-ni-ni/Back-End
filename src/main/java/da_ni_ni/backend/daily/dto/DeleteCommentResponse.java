package da_ni_ni.backend.daily.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import da_ni_ni.backend.daily.domain.Comment;
import da_ni_ni.backend.global.dto.ResponseDto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DeleteCommentResponse implements ResponseDto {
    private Long commentId;

    public static DeleteCommentResponse createWith(Comment comment) {
        return DeleteCommentResponse.builder()
                .commentId(comment.getId())
                .build();
    }
}