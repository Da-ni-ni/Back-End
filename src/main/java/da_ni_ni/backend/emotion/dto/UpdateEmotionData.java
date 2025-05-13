package da_ni_ni.backend.emotion.dto;

import da_ni_ni.backend.emotion.domain.EmotionType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateEmotionData {
    private String nickName;
    private EmotionType emotionType;

    public static UpdateEmotionData createWith(UpdateEmotionRequest request) {
        return UpdateEmotionData.builder()
                .nickName(request.getNickName())
                .emotionType(request.getEmotionType())
                .build();
    }

    public boolean hasNickname() {
        return this.nickName != null && !this.nickName.isBlank();
    }
}
