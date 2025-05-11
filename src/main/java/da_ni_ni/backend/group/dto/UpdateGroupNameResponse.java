package da_ni_ni.backend.group.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import da_ni_ni.backend.global.dto.ResponseDto;
import da_ni_ni.backend.group.domain.FamilyGroup;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;


@Getter
@Builder
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public class UpdateGroupNameResponse implements ResponseDto {
    private Long groupId;
    private String groupName;
    private LocalDateTime updatedAt;

    public static UpdateGroupNameResponse createWith(FamilyGroup familyGroup) {
        return UpdateGroupNameResponse.builder()
                .groupId(familyGroup.getId())
                .groupName(familyGroup.getName())
                .updatedAt(familyGroup.getUpdatedAt())
                .build();
    }
}
