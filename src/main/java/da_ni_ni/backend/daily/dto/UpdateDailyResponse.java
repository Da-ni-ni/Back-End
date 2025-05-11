package da_ni_ni.backend.daily.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import da_ni_ni.backend.daily.domain.Daily;
import da_ni_ni.backend.global.dto.ResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UpdateDailyResponse implements ResponseDto {
    private Long dailyId;
    private LocalDateTime updatedAt;

    public static UpdateDailyResponse createWith(Daily daily) {
        return UpdateDailyResponse.builder()
                .dailyId(daily.getId())
                .updatedAt(daily.getUpdatedAt())
                .build();
    }
}