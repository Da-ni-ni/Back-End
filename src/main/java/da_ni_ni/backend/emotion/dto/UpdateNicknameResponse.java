package da_ni_ni.backend.emotion.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import da_ni_ni.backend.common.ResponseDto;
import da_ni_ni.backend.emotion.domain.Emotion;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UpdateNicknameResponse implements ResponseDto {
    private Long emotionId;
    private String nickName;
    private LocalDateTime updatedAt;

    public static UpdateNicknameResponse createWith(Emotion emotion) {
        return UpdateNicknameResponse.builder()
                .emotionId(emotion.getId())
                .nickName(emotion.getUser().getNickName())
                .updatedAt(emotion.getUpdatedAt())
                .build();
    }
}
