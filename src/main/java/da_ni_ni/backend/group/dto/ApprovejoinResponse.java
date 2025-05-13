package da_ni_ni.backend.group.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import da_ni_ni.backend.common.ResponseDto;
import da_ni_ni.backend.group.domain.JoinReq;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ApprovejoinResponse implements ResponseDto {
    private Long requestId;
    private JoinReq.RequestStatus status;
    private LocalDateTime updatedAt;

    public static ApprovejoinResponse createWith(JoinReq join) {
        return ApprovejoinResponse.builder()
                .requestId(join.getId())
                .status(join.getStatus())
                .updatedAt(join.getUpdatedAt())
                .build();
    }
}
