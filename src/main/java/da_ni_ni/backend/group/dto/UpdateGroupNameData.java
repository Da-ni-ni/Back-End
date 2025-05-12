package da_ni_ni.backend.group.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateGroupNameData {
    private String newName;

    public static UpdateGroupNameData createWith(UpdateGroupNameRequest request) {
        return UpdateGroupNameData.builder()
                .newName(request.getNewName())
                .build();
    }
}
