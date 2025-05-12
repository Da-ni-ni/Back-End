package da_ni_ni.backend.group.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import da_ni_ni.backend.group.domain.JoinReq;
import lombok.Builder;
import lombok.Getter;

// 가입 요청 수락
@Getter
@Builder
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ApprovejoinRequest {
    private Long requestId;
    private JoinReq.RequestStatus status;
}
