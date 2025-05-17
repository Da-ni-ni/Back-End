package da_ni_ni.backend.qna.service;

import da_ni_ni.backend.qna.domain.DailyAnswer;
import da_ni_ni.backend.qna.domain.DailyQuestion;
import da_ni_ni.backend.qna.dto.*;
import da_ni_ni.backend.qna.exception.BadRequestException;
import da_ni_ni.backend.qna.exception.ForbiddenException;
import da_ni_ni.backend.qna.repository.DailyAnswerRepository;
import da_ni_ni.backend.qna.repository.DailyQuestionRepository;
import da_ni_ni.backend.user.domain.User;
import da_ni_ni.backend.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QnaService {
    private final DailyQuestionRepository questionRepo;
    private final DailyAnswerRepository answerRepo;
    private final AuthService authService;

    /** 1) 오늘 활성화된 질문 조회 (activation_date 기준) */
    public DailyQuestionDto getTodayQuestion() {
        User me = authService.getApprovedUser();
        LocalDate logicalDate = getLogicalDate();  // 오전 5시 기점 “오늘” 계산

        DailyQuestion q = questionRepo
                .findByActivationDate(logicalDate)
                .orElseThrow(() -> new BadRequestException("오늘의 질문이 아직 준비되지 않았습니다."));

        return new DailyQuestionDto(q.getId(), q.getQuestion());
    }

    /** 2) 월간 활성화 질문 조회 */
    public List<MonthlyQuestionDto> getMonthlyQuestions(int year, int month) {
        authService.getApprovedUser();

        LocalDate first = LocalDate.of(year, month, 1);
        LocalDate last  = first.plusMonths(1).minusDays(1);

        return questionRepo
                .findAllByActivationDateBetweenOrderByActivationDate(first, last)
                .stream()
                .map(q -> new MonthlyQuestionDto(
                        q.getId(),
                        q.getActivationDate().toString(),
                        q.getQuestion()
                ))
                .collect(Collectors.toList());
    }

    public QuestionDetailDto getQuestionDetail(Long questionId) {
        User me = authService.getApprovedUser();

        // 1) 우선 질문 자체가 있는지 검사 (ID 오류 → 400)
        DailyQuestion q = questionRepo.findById(questionId)
                .orElseThrow(() -> new BadRequestException("잘못된 질문 ID입니다."));

        // 2) 논리적 오늘 계산 (오전 5시 기준)
        LocalDate logicalToday = getLogicalDate();

        // 3) 질문의 activationDate 가져오기
        LocalDate activationDate = q.getActivationDate();

        // 4) “오늘” 활성화된 질문일 경우에만 본인 답변 여부 검사
        if (activationDate.equals(logicalToday)) {
            boolean answered = answerRepo.findByQuestionIdAndUserId(questionId, me.getId())
                    .isPresent();
            if (!answered) {
                throw new ForbiddenException(
                        "당일 질문에 답변을 등록해야 다른 가족의 답변을 볼 수 있습니다."
                );
            }
        }
        // activationDate < logicalToday (즉, 과거 질문)이면 답변 여부와 무관하게 통과

        // 5) 전체 가족의 답변 수집 (없는 사람은 “아직…” 텍스트)
        List<QuestionDetailDto.AnswerInfo> answers = authService.getFamilyMembers().stream()
                .map(member -> {
                    String ans = answerRepo
                            .findByQuestionIdAndUserId(questionId, member.getId())
                            .map(DailyAnswer::getAnswerText)
                            .orElse("아직 답변을 작성하지 않았습니다.");
                    return new QuestionDetailDto.AnswerInfo(
                            member.getId(),
                            member.getNickName(),
                            ans
                    );
                })
                .collect(Collectors.toList());

        // 6) DTO 반환
        return new QuestionDetailDto(
                q.getActivationDate().toString(), // 보통 createdAt이 아닌 activationDate를 보여줘도 좋습니다
                q.getId(),
                q.getQuestion(),
                answers
        );
    }


    // 4) 답변 등록
    @Transactional
    public String submitAnswer(Long questionId, AnswerRequestDto req) {
        User me = authService.getApprovedUser();
        LocalDate logicalToday = getLogicalDate();

        // 1) 오늘 활성화된 질문인지
        DailyQuestion activeQ = questionRepo.findByActivationDate(logicalToday)
                .orElseThrow(() -> new BadRequestException("오늘 활성화된 질문이 없습니다."));
        if (!activeQ.getId().equals(questionId)) {
            throw new BadRequestException("오늘 활성화된 질문에만 답변할 수 있습니다.");
        }

        // 2) 중복 답변 검사
        Long myId = me.getId();
        if (answerRepo.findByQuestionIdAndUserId(questionId, myId).isPresent()) {
            throw new BadRequestException("이미 답변을 등록했습니다.");
        }

        // 답변 저장
        DailyAnswer a = new DailyAnswer();
        a.setQuestion(activeQ);
        a.setUserId(myId);
        a.setAnswerText(req.getAnswer());
        answerRepo.save(a);
        return a.getCreatedAt().toString();
    }



    /** 5) 답변 수정 (당일 활성화된 질문에 대해서만) */
    @Transactional
    public String updateAnswer(Long questionId, AnswerRequestDto req) {
        User me = authService.getApprovedUser();
        Long myId = me.getId();

        // 1) 논리적 오늘 계산 (오전 5시 기준)
        LocalDate logicalToday = getLogicalDate();

        // 2) 오늘 활성화된 질문인지 확인
        DailyQuestion activeQ = questionRepo.findByActivationDate(logicalToday)
                .orElseThrow(() -> new BadRequestException("오늘 활성화된 질문이 없습니다."));
        if (!activeQ.getId().equals(questionId)) {
            throw new BadRequestException("오늘 활성화된 질문만 수정할 수 있습니다.");
        }

        // 3) 내 답변 로드 (없으면 400)
        DailyAnswer a = answerRepo.findByQuestionIdAndUserId(questionId, myId)
                .orElseThrow(() -> new BadRequestException("내 답변이 없습니다."));

        // 4) 답변이 “오늘” 작성된 것인지 검증
        LocalDateTime createdAt = a.getCreatedAt();
        LocalDate creationLogicalDate = createdAt.toLocalTime().isBefore(LocalTime.of(5, 0))
                ? createdAt.toLocalDate().minusDays(1)
                : createdAt.toLocalDate();
        if (!creationLogicalDate.equals(logicalToday)) {
            throw new BadRequestException("당일(오전 5시 이후) 등록한 답변만 수정할 수 있습니다.");
        }

        // 5) 실제 답변 수정
        a.setAnswerText(req.getAnswer());
        DailyAnswer updated = answerRepo.saveAndFlush(a);

        return updated.getUpdatedAt().toString();
    }


    /** 6) 답변 삭제 (당일 활성화된 질문에 대해서만) */
    @Transactional
    public Long deleteAnswer(Long questionId) {
        User me = authService.getApprovedUser();
        Long myId = me.getId();

        // 1) 논리적 오늘 계산 (오전 5시 기준)
        LocalDate logicalToday = getLogicalDate();

        // 2) 오늘 활성화된 질문인지 확인
        DailyQuestion activeQ = questionRepo.findByActivationDate(logicalToday)
                .orElseThrow(() -> new BadRequestException("오늘 활성화된 질문이 없습니다."));
        if (!activeQ.getId().equals(questionId)) {
            throw new BadRequestException("오늘 활성화된 질문만 삭제할 수 있습니다.");
        }

        // 3) 내 답변이 실제로 있는지 확인
        DailyAnswer a = answerRepo.findByQuestionIdAndUserId(questionId, myId)
                .orElseThrow(() -> new BadRequestException("내 답변이 없습니다."));

        // 4) 삭제
        answerRepo.deleteByQuestionIdAndUserId(questionId, myId);
        return questionId;
    }


    /** “오늘”을 오전 5시 기점으로 계산하는 헬퍼 */
    private LocalDate getLogicalDate() {
        ZoneId zone = ZoneId.of("Asia/Seoul");
        LocalTime now = LocalTime.now(zone);
        LocalDate today = LocalDate.now(zone);
        // 오전 5시 전이면 ‘어제’를 오늘로 본다
        return now.isBefore(LocalTime.of(5, 0))
                ? today.minusDays(1)
                : today;
    }
}
