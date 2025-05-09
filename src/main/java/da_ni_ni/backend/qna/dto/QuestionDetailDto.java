package da_ni_ni.backend.qna.dto;

import java.util.List;

public record QuestionDetailDto(
        String date,
        Long dailyId,
        String dailyQuestion,
        List<AnswerInfo> answers
) {
    public record AnswerInfo(Long memberId, String memberName, String answer) {}
}