package da_ni_ni.backend.group.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import da_ni_ni.backend.global.dto.ResponseDto;
import da_ni_ni.backend.group.domain.Group;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CreateGroupResponse implements ResponseDto {
    private Long groupId;
    private String inviteCode;
    private LocalDateTime createdAt;

    public static CreateGroupResponse createWith(Group group) {
        return CreateGroupResponse.builder()
                .groupId(group.getId())
                .inviteCode(group.getInviteCode())
                .createdAt(group.getCreatedAt())
                .build();
    }
}
