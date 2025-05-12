package da_ni_ni.backend.user.dto;

import da_ni_ni.backend.common.ResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EmailCheckResponseDto implements ResponseDto {
    private boolean duplicated;
}