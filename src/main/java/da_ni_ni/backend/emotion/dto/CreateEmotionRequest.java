package da_ni_ni.backend.emotion.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import da_ni_ni.backend.emotion.domain.EmotionType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateEmotionRequest {
    private LocalDate date;
    private EmotionType emotion;
}
