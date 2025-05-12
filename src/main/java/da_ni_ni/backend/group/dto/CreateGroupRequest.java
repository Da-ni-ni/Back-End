package da_ni_ni.backend.group.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CreateGroupRequest {
    @NotBlank(message = "가족명을 입력해주세요.")
    private String groupName;
}
