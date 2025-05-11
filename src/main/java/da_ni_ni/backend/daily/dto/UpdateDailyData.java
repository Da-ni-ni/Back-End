package da_ni_ni.backend.daily.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class UpdateDailyData {
    private LocalDate date;
    private String content;


    public static UpdateDailyData createWith(UpdateDailyRequest request) {
        return UpdateDailyData.builder()
                .date(request.getDate())
                .content(request.getContent())
                .build();
    }
}
