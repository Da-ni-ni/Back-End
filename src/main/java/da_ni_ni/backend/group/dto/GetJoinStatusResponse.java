package da_ni_ni.backend.group.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import da_ni_ni.backend.common.ResponseDto;
import da_ni_ni.backend.group.domain.JoinReq;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 가입 요청 상태
@Getter
@Builder
@AllArgsConstructor
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public class GetJoinStatusResponse implements ResponseDto {
    private Long requestId;
    private Long groupId;
    private String userName;
    private JoinReq.RequestStatus status;
    private LocalDateTime createdAt;

    public static GetJoinStatusResponse createWith(JoinReq join) {
        Long approvedGroupId = null;

        if (join.getStatus() == JoinReq.RequestStatus.APPROVED) {
            approvedGroupId = join.getFamilyGroup().getId();
        }

        return GetJoinStatusResponse.builder()
                .requestId(join.getId())
                .groupId(approvedGroupId)
                .userName(join.getUser().getName())
                .status(join.getStatus())
                .createdAt(join.getCreatedAt())
                .build();
    }
}
