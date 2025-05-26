package da_ni_ni.backend.qna.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import da_ni_ni.backend.qna.dto.AnswerRequestDto;
import da_ni_ni.backend.qna.dto.DailyQuestionDto;
import da_ni_ni.backend.qna.dto.MonthlyQuestionDto;
import da_ni_ni.backend.qna.dto.QuestionDetailDto;
import da_ni_ni.backend.qna.service.QnaService;
import da_ni_ni.backend.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/question")
@RequiredArgsConstructor
public class QnaController {

    private final QnaService qnaService;
    private final AuthService authService;

    /** 1) 일일 질문 생성 */
    @GetMapping("/everyday")
    public ResponseEntity<QuestionResponseDto> getTodayQuestion() {
        DailyQuestionDto dto = qnaService.getTodayQuestion();
        return ResponseEntity.ok(new QuestionResponseDto(dto.dailyId(), dto.dailyQuestion()));
    }

    /** 2) 월간 질문 조회 */
    @GetMapping("/monthly")
    public ResponseEntity<List<MonthlyQuestionResponseDto>> getMonthlyQuestions(
            @RequestParam int year,
            @RequestParam int month
    ) {
        List<MonthlyQuestionDto> list = qnaService.getMonthlyQuestions(year, month);
        List<MonthlyQuestionResponseDto> resp = list.stream()
                .map(q -> new MonthlyQuestionResponseDto(
                        q.dailyId(),
                        q.date(),
                        q.dailyQuestion()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(resp);
    }

    /** 3) 문답 상세 조회 */
    @GetMapping("/{questionId}")
    public ResponseEntity<QuestionDetailResponseDto> getQuestionDetail(
            @PathVariable Long questionId
    ) {
        QuestionDetailDto dto = qnaService.getQuestionDetail(questionId);
        List<QuestionDetailResponseDto.AnswerInfo> answers = dto.answers().stream()
                .map(a -> new QuestionDetailResponseDto.AnswerInfo(
                        a.memberId(), a.memberName(), a.answer()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(new QuestionDetailResponseDto(
                dto.date(),
                dto.dailyId(),
                dto.dailyQuestion(),
                answers
        ));
    }

    /** 4) 일일 답변 등록 */
    @PostMapping("/{questionId}/answers")
    public ResponseEntity<CreatedResponse> submitAnswer(
            @PathVariable Long questionId,
            @RequestBody @Valid AnswerRequestDto req
    ) {
        String createdAt = qnaService.submitAnswer(questionId, req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreatedResponse(createdAt));
    }

    /** 5) 일일 답변 수정 */
    @PutMapping("/{questionId}/answers")
    public ResponseEntity<UpdatedResponse> updateAnswer(
            @PathVariable Long questionId,
            @RequestBody @Valid AnswerRequestDto req
    ) {
        // 서비스에서 내부적으로 getLogicalDate()를 사용해
        // “오늘 활성화된 질문인지”와 “내 답변 생성 시점이 논리적 오늘인지”를 모두 검증합니다.
        String updatedAt = qnaService.updateAnswer(questionId, req);

        Long userId = authService.getCurrentUser().getId();
        UpdatedResponse body = new UpdatedResponse(
                questionId,
                userId,
                req.getAnswer(),
                updatedAt
        );
        return ResponseEntity.ok(body);
    }

    /** 6) 일일 답변 삭제 */
    @DeleteMapping("/{questionId}/answers")
    public ResponseEntity<DeletedResponse> deleteAnswer(
            @PathVariable Long questionId
    ) {
        Long deletedId = qnaService.deleteAnswer(questionId);
        return ResponseEntity.ok(new DeletedResponse(deletedId));
    }

    // —— DTOs for controller responses —— //

    // 수정할 코드:
    public record QuestionResponseDto(
            @JsonProperty("question_id") Long questionId,
            String question
    ) {}

    public record MonthlyQuestionResponseDto(
            @JsonProperty("question_id") Long questionId,
            String date,
            String question
    ) {}

    public record QuestionDetailResponseDto(
            String date,
            @JsonProperty("question_id") Long questionId,
            @JsonProperty("daily_question") String question,
            List<AnswerInfo> answers
    ) {
        public record AnswerInfo(
                @JsonProperty("user_id") Long userId,
                String nickname,
                String answer
        ) {}
    }

    public record CreatedResponse(String createdAt) {}

    public record UpdatedResponse(
            @JsonProperty("question_id") Long questionId,
            @JsonProperty("user_id") Long userId,
            String answer,
            String updatedAt
    ) {}

    public record DeletedResponse(
            @JsonProperty("question_id") Long questionId
    ) {}
}