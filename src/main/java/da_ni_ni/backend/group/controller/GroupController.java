package da_ni_ni.backend.group.controller;

import da_ni_ni.backend.common.ResponseDto;
import da_ni_ni.backend.group.dto.*;
import da_ni_ni.backend.group.service.GroupService;
import da_ni_ni.backend.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/groups")
public class GroupController {

    private final GroupService groupService;
    private final AuthService authService;

    // 그룹 생성
    @PostMapping
    public ResponseEntity<ResponseDto> createGroup (
            @RequestBody CreateGroupRequest request) {
        log.info("Request to POST familyGroup");
        Long userId = authService.getCurrentUser().getId();
        CreateGroupResponse response = groupService.createGroup(userId, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 가입 요청
    @PostMapping("/join-request")
    public ResponseEntity<ResponseDto> requestJoin (
            @RequestBody JoinGroupRequest request) {
        log.info("Request to POST join request");
        Long userId = authService.getCurrentUser().getId();
        JoinGroupResponse response = groupService.requestJoin(userId, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

/*    // 내 가입 요청 상태 조회
    @GetMapping("/join-request/me")
    public ResponseEntity<ResponseDto> getMyRequestStatus (@RequestHeader("Authorization") String authHeader) {
        log.info("Request to Get my request status");
        Long userId = authService.getUserFromHeader(authHeader).getId();
        GetJoinStatusResponse response = groupService.getMyJoinStatus(userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }*/

    // 가입 요청 목록 조회 (생성자만)
    @GetMapping("/join-request")
    public ResponseEntity<ResponseDto> getJoinRequestList () {
        log.info("Request to Get request list");
        Long userId = authService.getCurrentUser().getId();
        GetJoinStatusListResponse response = groupService.getJoinRequestsList(userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 가입 요청 수락 (생성자만)
    @PostMapping("/join-accept")
    public ResponseEntity<ResponseDto> acceptJoinRequest (
            @RequestBody ApprovejoinRequest request) {
        log.info("Request to POST request accept");
        Long userId = authService.getCurrentUser().getId();
        ApprovejoinResponse response = groupService.approveJoinRequest(userId, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 그룹명 수정
    @PutMapping
    private ResponseEntity<ResponseDto> updateGroupName (
            @RequestBody UpdateGroupNameRequest request) {
        log.info("Request to PUT familyGroup name");
        Long userId = authService.getCurrentUser().getId();
        UpdateGroupNameResponse response = groupService.updateGroupName(userId, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
