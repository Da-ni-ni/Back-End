package da_ni_ni.backend.emotion.controller;

import da_ni_ni.backend.common.ResponseDto;
import da_ni_ni.backend.emotion.dto.*;
import da_ni_ni.backend.emotion.service.EmotionService;
import da_ni_ni.backend.group.dto.UpdateGroupNameRequest;
import da_ni_ni.backend.group.dto.UpdateGroupNameResponse;
import da_ni_ni.backend.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/emotions")
public class EmotionController {

    private final EmotionService emotionService;
    private final AuthService authService;

    // 내 감정 추가
    @PostMapping
    public ResponseEntity<ResponseDto> createEmotion(
            @RequestBody CreateEmotionRequest request) {
        log.info("Request to POST create emotion");
        Long userId = authService.getCurrentUser().getId();
        CreateEmotionResponse response = emotionService.addEmotion(request, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // 내 감정 수정
    @PutMapping
    public ResponseEntity<ResponseDto> updateEmotion(
            @RequestBody UpdateEmotionRequest request) {
        log.info("Request to PUT update emotion");
        Long userId = authService.getCurrentUser().getId();
        UpdateEmotionResponse response = emotionService.updateEmotionResponse(request, userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 감정 상세 조회
    @GetMapping("/{emotionId}")
    public ResponseEntity<ResponseDto> getEmotionDetail(
            @PathVariable Long emotionId) {
        log.info("Request to GET emotion detail");
        Long userId = authService.getCurrentUser().getId();
        FindEmotionDetailResponse response = emotionService.getEmotionDetail(emotionId, userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 가족 전체 감정 조회
    @GetMapping("/group/{groupId}")
    public ResponseEntity<ResponseDto> getGroupEmotions(
            @PathVariable Long groupId) {
        log.info("Request to GET group emotions");
        FindTotalEmotionsResponse response = emotionService.getGroupEmotions(groupId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 다른 구성원 닉네임 수정
    @PutMapping("/{emotionId}")
    public ResponseEntity<ResponseDto> updateOtherMemberNickname(
            @PathVariable Long emotionId,
            @RequestBody UpdateNicknameRequest request) {
        log.info("Request to PUT update other member nickname");
        Long userId = authService.getCurrentUser().getId();
        UpdateNicknameResponse response = emotionService.updateOtherMemberNickname(request, emotionId, userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 그룹명 수정
    @PutMapping("/group/{groupId}/groupname")
    private ResponseEntity<ResponseDto> updateGroupName (
            @RequestBody UpdateGroupNameRequest request) {
        log.info("Request to PUT familyGroup name");
        Long userId = authService.getCurrentUser().getId();
        UpdateGroupNameResponse response =emotionService.updateGroupName(userId, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
