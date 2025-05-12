package da_ni_ni.backend.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor            // Jackson 역직렬화용 기본 생성자
@AllArgsConstructor           // 필드 설정용 생성자
public class ErrorResponseDto {
    private int status;
    private String message;
}