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
    private Long emotionId;
    private String nickName;
    private EmotionType emotionType;
    private LocalDateTime updatedAt;

    public static FindEmotionDetailResponse createWith(Emotion emotion) {
        return FindEmotionDetailResponse.builder()
                .emotionId(emotion.getId())
                .nickName(emotion.getUser().getNickName())
                .emotionType(emotion.getEmotionType())
                .updatedAt(emotion.getUpdatedAt())
                .build();
    }
}
