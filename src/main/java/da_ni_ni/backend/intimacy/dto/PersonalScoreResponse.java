package da_ni_ni.backend.intimacy.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PersonalScoreResponse {
    private String memberName;  // ex: "나"
    private Integer score;      // 0~100
}