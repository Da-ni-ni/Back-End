package da_ni_ni.backend.daily.dto;

import da_ni_ni.backend.global.dto.ResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FindWeekDailyResponse implements ResponseDto {
    private List<DailySimpleData> dailyList;

    public static FindWeekDailyResponse createWith(List<DailySimpleData> dailyList) {
        return FindWeekDailyResponse.builder()
                .dailyList(dailyList)
                .build();
    }
}
