package da_ni_ni.backend.daily.controller;

import da_ni_ni.backend.daily.dto.*;
import da_ni_ni.backend.daily.service.DailyService;
import da_ni_ni.backend.common.ResponseDto;
import da_ni_ni.backend.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/daily")
public class DailyController {

    private final DailyService dailyService;
    private final AuthService authService;

    // 게시글 추가
    @PostMapping
    public ResponseEntity<ResponseDto> addDailyPost(
            @RequestBody CreateDailyRequest request) {
        log.info("Request to POST daily");
        Long userId = authService.getCurrentUser().getId();
        CreateDailyResponse response = dailyService.addDaily(request, userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 게시글 수정
    @PutMapping("/{dailyId}")
    public ResponseEntity<ResponseDto> updateDailyPost(
            @PathVariable("dailyId") Long dailyId,
            @RequestBody UpdateDailyRequest request) {
        log.info("Request to PUT daily");
        Long userId = authService.getCurrentUser().getId();
        UpdateDailyResponse response = dailyService.updateDaily(request, dailyId, userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 게시글 삭제
    @DeleteMapping("/{dailyId}")
    public ResponseEntity<ResponseDto> deleteDailyPost(
            @PathVariable("dailyId") Long dailyId) {
        log.info("Request to DELETE daily");
        Long userId = authService.getCurrentUser().getId();
        DeleteDailyResponse response = dailyService.deleteDaily(dailyId, userId);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    // 댓글 추가
    @PostMapping("/{dailyId}/comments")
    public ResponseEntity<ResponseDto> addDailyComment(
            @PathVariable("dailyId") Long dailyId,
            @RequestBody CreateCommentRequest request) {
        log.info("Request to POST comment");
        Long userId = authService.getCurrentUser().getId();
        CreateCommentResponse response = dailyService.addComment(request, dailyId, userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 댓글 수정
    @PutMapping("/{dailyId}/comments/{commentId}")
    public ResponseEntity<ResponseDto> updateDailyComment(
            @PathVariable("dailyId") Long dailyId,
            @PathVariable("commentId") Long commentId,
            @RequestBody UpdateCommentRequest request) {
        log.info("Request to PUT comment");
        Long userId = authService.getCurrentUser().getId();
        UpdateCommentResponse response = dailyService.updateComment(request, dailyId, commentId, userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 댓글 삭제
    @DeleteMapping("/{dailyId}/comments/{commentId}")
    public ResponseEntity<ResponseDto> deleteDailyComment(
            @PathVariable("dailyId") Long dailyId,
            @PathVariable("commentId") Long commentId)  {
        log.info("Request to DELETE comment");
        Long userId = authService.getCurrentUser().getId();
        DeleteCommentResponse response = dailyService.deleteComment(dailyId, commentId, userId);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    // 좋아요 토글
    @PostMapping("/{dailyId}")
    public ResponseEntity<ResponseDto> toggleLike(
            @PathVariable("dailyId") Long dailyId) {
        log.info("Request to TOGGLE like");
        Long userId = authService.getCurrentUser().getId();
        ToggleLikeResponse response = dailyService.toggleLike(dailyId, userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 게시글 상세 조회
    @GetMapping("/{dailyId}")
    public ResponseEntity<ResponseDto> getDailyPostDetails(
            @PathVariable("dailyId") Long dailyId) {
        log.info("Request to GET daily details");
        Long userId = authService.getCurrentUser().getId();
        FindDailyDetailResponse response = dailyService.getDailyDetail(dailyId, userId);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    // 주간 게시글 조회
    @GetMapping
    public ResponseEntity<ResponseDto> getWeeklyDailies(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        log.info("Request to GET weekly dailies with date: {}", date);
        Long userId = authService.getCurrentUser().getId();
        // 기준일이 없으면 오늘 날짜 사용
        if (date == null) {
            date = LocalDate.now();
        }
        FindWeekDailyResponse response = dailyService.getWeeklyDailies(userId, date);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

}


