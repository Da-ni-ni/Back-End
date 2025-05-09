package da_ni_ni.backend.qna.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AnswerRequestDto {
    @NotBlank
    @Size(max = 150)
    private String answer;
    // getter / setter

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
