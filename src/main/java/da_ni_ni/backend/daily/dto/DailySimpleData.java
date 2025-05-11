package da_ni_ni.backend.daily.dto;

import da_ni_ni.backend.daily.domain.Comment;
import da_ni_ni.backend.daily.domain.Daily;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class DailySimpleData {
    private Long dailyId;
    private LocalDate date;
    private String content;
    private long likeCount;
    private List<Comment> comments;

    public static DailySimpleData createWith(Daily daily, long likeCount, long commentCount) {
        return DailySimpleData.builder()
                .dailyId(daily.getId())
                .date(daily.getDate())
                .content(daily.getContent())
                .likeCount(likeCount)
                .comments(daily.getComments())
                .build();
    }
}
