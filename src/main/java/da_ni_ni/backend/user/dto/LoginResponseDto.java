package da_ni_ni.backend.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class LoginResponseDto {
    private String name;
    private String email;
    private String token;
    private String refreshToken;
    private Long familyGroupId;       // 그룹 ID (없으면 null)
    private boolean hasIntimacyTest;  // 친밀도 검사 결과 존재 여부

}