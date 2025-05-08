package da_ni_ni.backend.qna.dto;

public record MonthlyQuestionDto(
        Long dailyId,
        String date,
        String dailyQuestion
) {}