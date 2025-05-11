package da_ni_ni.backend.emotion.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateNicknameData {
    private String nickName;

    public static UpdateNicknameData createWith(UpdateNicknameRequest request) {
        return UpdateNicknameData.builder()
                .nickName(request.getNickName())
                .build();
    }

}
