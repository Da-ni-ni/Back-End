package da_ni_ni.backend.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EmailCheckResponseDto {
    private boolean duplicated;
}
