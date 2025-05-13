package da_ni_ni.backend.daily.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import da_ni_ni.backend.daily.domain.Comment;
import da_ni_ni.backend.daily.domain.Daily;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public class DailySimpleData {
    private Long dailyId;
    private LocalDateTime date;
    private String authorName;
    private String content;
    private long likeCount;
    private long commentCount;

    public static DailySimpleData createWith(Daily daily, long likeCount, long commentCount) {
        return DailySimpleData.builder()
                .dailyId(daily.getId())
                .date(daily.getCreatedAt())
                .authorName(daily.getUser().getNickName() != null ? daily.getUser().getNickName() : daily.getUser().getName())
                .content(daily.getContent())
                .likeCount(likeCount)
                .commentCount(commentCount)
                .build();
    }
}
