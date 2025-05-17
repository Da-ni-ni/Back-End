package da_ni_ni.backend.intimacy.controller;

import da_ni_ni.backend.intimacy.dto.FamilyScoreResponse;
import da_ni_ni.backend.intimacy.dto.PersonalScoreResponse;
import da_ni_ni.backend.intimacy.dto.TestAnswerRequest;
import da_ni_ni.backend.intimacy.service.IntimacyService;
import da_ni_ni.backend.user.domain.User;
import da_ni_ni.backend.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tests")
@RequiredArgsConstructor
public class IntimacyController {
    private final IntimacyService intimacyService;
    private final AuthService authService;

    /** 1) 테스트 답변 제출 */
    @PostMapping("/answers")
    public ResponseEntity<Void> submitAnswers(@Valid @RequestBody TestAnswerRequest request) {
        User me = authService.getCurrentUser();
        intimacyService.submitAnswers(me, request.getAnswers());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /** 2) 개인 점수 조회 */
    @GetMapping("/personal")
    public ResponseEntity<PersonalScoreResponse> getPersonal() {
        User me = authService.getCurrentUser();
        return ResponseEntity.ok(intimacyService.getPersonalScore(me));
    }

    /** 3) 가족 점수 조회 */
    @GetMapping("/family")
    public ResponseEntity<FamilyScoreResponse> getFamily() {
        User me = authService.getCurrentUser();
        return ResponseEntity.ok(intimacyService.getFamilyScore(me));
    }
}
