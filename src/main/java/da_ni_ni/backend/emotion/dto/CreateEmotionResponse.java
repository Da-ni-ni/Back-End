package da_ni_ni.backend.emotion.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import da_ni_ni.backend.common.ResponseDto;
import da_ni_ni.backend.emotion.domain.Emotion;
import da_ni_ni.backend.emotion.domain.EmotionType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateEmotionResponse implements ResponseDto {
    private Long emotionId;
    private EmotionType emotionType;
    private LocalDateTime createdAt;

    public static CreateEmotionResponse createWith(Emotion emotion) {
        return CreateEmotionResponse.builder()
                .emotionId(emotion.getId())
                .emotionType(emotion.getEmotionType())
                .createdAt(emotion.getCreatedAt())
                .build();
    }
}
