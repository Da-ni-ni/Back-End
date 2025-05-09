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
public class UpdateGroupNameResponse implements ResponseDto {
    private Long groupId;
    private String groupName;
    private LocalDateTime updatedAt;

    public static UpdateGroupNameResponse createWith(Group group) {
        return UpdateGroupNameResponse.builder()
                .groupId(group.getId())
                .groupName(group.getName())
                .updatedAt(group.getUpdatedAt())
                .build();
    }
}
