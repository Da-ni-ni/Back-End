package da_ni_ni.backend.daily.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import da_ni_ni.backend.daily.domain.Daily;
import da_ni_ni.backend.common.ResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public class FindDailyDetailResponse implements ResponseDto {
    private Long dailyId;
    private LocalDate date;
    private String authorName;
    private String content;
    private long likeCount;
    private long commentCount;
    private List<CommentDetailData> comments;

    public static FindDailyDetailResponse createWith(Daily daily, List<CommentDetailData> comments) {
        return FindDailyDetailResponse.builder()
                .dailyId(daily.getId())
                .date(daily.getDate())
                .authorName(daily.getUser().getNickName())
                .content(daily.getContent())
                .likeCount(daily.getLikeCount())
                .commentCount(daily.getCommentCount())
                .comments(comments)
                .build();


    }


}
