package da_ni_ni.backend.intimacy.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FamilyScoreResponse {
    private String familyName;
    private Double averageScore;
}