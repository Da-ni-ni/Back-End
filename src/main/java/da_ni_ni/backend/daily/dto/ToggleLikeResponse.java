package da_ni_ni.backend.daily.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import da_ni_ni.backend.common.ResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.transaction.annotation.Transactional;

@Getter
@Builder
@AllArgsConstructor
@Transactional
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ToggleLikeResponse implements ResponseDto {
    private boolean liked; // ture = 좋아요 추가, false = 좋아요 삭제
}
