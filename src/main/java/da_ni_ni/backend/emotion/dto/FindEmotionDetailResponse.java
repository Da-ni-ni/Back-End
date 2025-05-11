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
public class FindEmotionDetailResponse implements ResponseDto {
    private Long userId;
    private String name;
    private EmotionType emotion;
    private LocalDateTime updatedAt;

    public static FindEmotionDetailResponse createWith(Emotion emotion) {
        return FindEmotionDetailResponse.builder()
                .userId(emotion.getUser().getId())
                .name(emotion.getUser().getNickName())
                .emotion(emotion.getType())
                .updatedAt(emotion.getUpdatedAt())
                .build();
    }
}
