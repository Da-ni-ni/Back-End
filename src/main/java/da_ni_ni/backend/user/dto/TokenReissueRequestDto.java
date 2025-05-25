package da_ni_ni.backend.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenReissueRequestDto {
    private String refreshToken;
}