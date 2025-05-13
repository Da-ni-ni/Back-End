package da_ni_ni.backend.emotion.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import da_ni_ni.backend.emotion.domain.EmotionType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UpdateEmotionRequest {
    private String nickName; // Null 값 가능 (초기값 유지)
    private EmotionType emotionType;
}
