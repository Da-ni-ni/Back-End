package da_ni_ni.backend.emotion.dto;

import da_ni_ni.backend.emotion.domain.EmotionType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateEmotionData {
    private String nickName;
    private EmotionType emotion;

    public static UpdateEmotionData createWith(UpdateEmotionRequest request) {
        return UpdateEmotionData.builder()
                .nickName(request.getNickName())
                .emotion(request.getEmotion())
                .build();
    }

    public boolean hasNickname() {
        return this.nickName != null && !this.nickName.isBlank();
    }
}
