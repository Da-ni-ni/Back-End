package da_ni_ni.backend.daily.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class UpdateCommentData {
    private LocalDate date;
    private String content;


    public static UpdateCommentData createWith(UpdateCommentRequest request) {
        return UpdateCommentData.builder()
                .date(request.getDate())
                .content(request.getContent())
                .build();
    }
}
