package da_ni_ni.backend.daily.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import da_ni_ni.backend.daily.domain.Daily;
import da_ni_ni.backend.common.ResponseDto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DeleteDailyResponse implements ResponseDto {
    private Long dailyId;

    public static DeleteDailyResponse createWith(Daily daily) {
        return DeleteDailyResponse.builder()
                .dailyId(daily.getId())
                .build();
    }
}
