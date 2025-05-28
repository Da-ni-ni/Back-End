package da_ni_ni.backend.emotion.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import da_ni_ni.backend.common.ResponseDto;
import da_ni_ni.backend.group.domain.FamilyGroup;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class FindTotalEmotionsResponse implements ResponseDto {
    private String groupName;
    private List<FindEmotionDetailResponse> emotionList;

    public static FindTotalEmotionsResponse createWith(FamilyGroup familyGroup
                                                       , List<FindEmotionDetailResponse> emotionList) {
        return FindTotalEmotionsResponse.builder()
                .groupName(familyGroup.getName())
                .emotionList(emotionList)
                .build();
    }
}
