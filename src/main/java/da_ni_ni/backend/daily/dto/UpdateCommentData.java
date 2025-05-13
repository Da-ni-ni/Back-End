package da_ni_ni.backend.daily.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class UpdateCommentData {
    private String content;


    public static UpdateCommentData createWith(UpdateCommentRequest request) {
        return UpdateCommentData.builder()
                .content(request.getContent())
                .build();
    }
}
