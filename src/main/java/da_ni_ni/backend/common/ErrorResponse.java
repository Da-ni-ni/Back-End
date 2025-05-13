package da_ni_ni.backend.common;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class ErrorResponse {
    private Map<String, String> errorMessages;
}
