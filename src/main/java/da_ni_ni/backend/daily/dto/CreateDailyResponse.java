package da_ni_ni.backend.daily.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import da_ni_ni.backend.daily.domain.Daily;
import da_ni_ni.backend.common.ResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateDailyResponse implements ResponseDto {
    private Long dailyId;
    private LocalDateTime createdAt;

    public static CreateDailyResponse createWith(Daily daily) {
        return CreateDailyResponse.builder()
                .dailyId(daily.getId())
                .createdAt(daily.getCreatedAt())
                .build();
    }
}
