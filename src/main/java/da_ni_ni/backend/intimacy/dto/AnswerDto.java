package da_ni_ni.backend.intimacy.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AnswerDto {
    private Integer questionId;
    private Integer answer;  // 1~5
}