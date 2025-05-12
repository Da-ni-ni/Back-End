package da_ni_ni.backend.group.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public class UpdateGroupNameRequest {
    private String newName;
}
