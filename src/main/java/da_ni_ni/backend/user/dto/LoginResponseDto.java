package da_ni_ni.backend.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponseDto {
    private String name;
    private String email;
    private String token;
    private Long groupId;     // 가입된 그룹이 없으면 null
}
