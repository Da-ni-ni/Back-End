package da_ni_ni.backend.group.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import da_ni_ni.backend.common.ResponseDto;
import da_ni_ni.backend.group.domain.JoinReq;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 가입 요청 응답
@Getter
@Builder
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public class JoinGroupResponse implements ResponseDto {
    private Long requestId;
    private JoinReq.RequestStatus status;
    private LocalDateTime createdAt;

    public static JoinGroupResponse createWith(JoinReq join) {
        return JoinGroupResponse.builder()
                .requestId(join.getId())
                .status(join.getStatus())
                .createdAt(join.getCreatedAt())
                .build();
    }

}
