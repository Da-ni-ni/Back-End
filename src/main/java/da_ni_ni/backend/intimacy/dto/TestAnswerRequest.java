package da_ni_ni.backend.intimacy.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TestAnswerRequest {
    /** 반드시 10개, answer는 1~5 */
    @Size(min = 10, max = 10)
    private List<AnswerDto> answers;
}


