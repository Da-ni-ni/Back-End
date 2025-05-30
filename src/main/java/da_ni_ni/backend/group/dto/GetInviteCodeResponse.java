package da_ni_ni.backend.group.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import da_ni_ni.backend.common.ResponseDto;
import da_ni_ni.backend.group.domain.FamilyGroup;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

;

@Getter
@Builder
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public class GetInviteCodeResponse implements ResponseDto {
    private Long groupId;
    private String inviteCode;
    private String groupName;
    private LocalDateTime createdAt;

    public static GetInviteCodeResponse createWith(FamilyGroup familyGroup) {
        return GetInviteCodeResponse.builder()
                .groupId(familyGroup.getId())
                .inviteCode(familyGroup.getInviteCode())
                .groupName(familyGroup.getName())
                .createdAt(familyGroup.getCreatedAt())
                .build();
    }
}