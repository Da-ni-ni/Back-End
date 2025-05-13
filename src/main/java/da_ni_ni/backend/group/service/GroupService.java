package da_ni_ni.backend.group.service;

import da_ni_ni.backend.group.domain.FamilyGroup;
import da_ni_ni.backend.group.domain.JoinReq;
import da_ni_ni.backend.group.dto.*;
import da_ni_ni.backend.group.exception.*;
import da_ni_ni.backend.group.repository.JoinRequestRepository;
import da_ni_ni.backend.group.repository.GroupRepository;
import da_ni_ni.backend.user.domain.User;
import da_ni_ni.backend.user.exception.UserNotFoundException;
import da_ni_ni.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final JoinRequestRepository joinRequestRepository;
    private final UserRepository userRepository;

    // 그룹 생성 (O)
    public CreateGroupResponse createGroup(Long userId, CreateGroupRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 이미 만든 그룹이 있는지 확인
        if (groupRepository.existsByAdminUser(user)) {
            throw new IllegalStateException("사용자는 하나의 그룹만 생성할 수 있습니다.");
        }
        FamilyGroup familyGroup = FamilyGroup.create(request.getGroupName(), user);
        familyGroup.addUser(user);
        groupRepository.save(familyGroup);
        return CreateGroupResponse.createWith(familyGroup);
    }

    // 가입 요청
    public JoinGroupResponse requestJoin(Long userId, JoinGroupRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        FamilyGroup familyGroup = groupRepository.findByInviteCode(request.getInviteCode())
                .orElseThrow(InviteCodeNotFoundException::new);
        // 이미 가입이 된 유저 예외 처리
        if (user.getFamilyGroup() != null) throw new AlreadyInGroupException();

        // 가입 요청 생성
        JoinReq joinReq = JoinReq.builder()
                .user(user)
                .familyGroup(familyGroup)
                .inviteCode(request.getInviteCode())
                .status(JoinReq.RequestStatus.PENDING)
                .build();
        joinRequestRepository.save(joinReq);

        return JoinGroupResponse.createWith(joinReq);
    }

    // 내 가입 요청 상태 조회
    public GetJoinStatusResponse getMyJoinStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        JoinReq joinReq = joinRequestRepository.findByUser(user)
                .orElseThrow(InvitationRequestNotFoundException::new);
        return GetJoinStatusResponse.createWith(joinReq);
    }

    // 가입 요청 목록 조회 (생성자만 확인 가능) (O)
    public GetJoinStatusListResponse getJoinRequestsList(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        FamilyGroup familyGroup = groupRepository.findByAdminUser(user)
                .orElseThrow(GroupNotFoundException::new);

        List<JoinReq> requests = joinRequestRepository.findAllByInviteCode(familyGroup.getInviteCode());
        List<GetJoinStatusResponse> list = requests.stream()
                .map(req -> new GetJoinStatusResponse(
                        req.getId(),
                        req.getUser().getName(),
                        req.getStatus(),
                        req.getCreatedAt()
                )).collect(Collectors.toList());

        return new GetJoinStatusListResponse(list);
    }

    // 가입 요청 처리 (생성자만) (O)
    public ApprovejoinResponse approveJoinRequest(Long userId, ApprovejoinRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        FamilyGroup familyGroup = groupRepository.findByAdminUser(user)
                .orElseThrow(GroupNotFoundException::new);
        List<JoinReq> joinReq = joinRequestRepository.findAllByInviteCode(familyGroup.getInviteCode());
        if (joinReq.isEmpty()) {
            throw new InvitationRequestNotFoundException();
        }
        JoinReq targetRequest = joinReq.stream()
                .filter(req -> req.getId().equals(request.getRequestId()))
                .findFirst()
                .orElseThrow(InvitationRequestNotFoundException::new);

        // 요청 수락
        if (request.getStatus() == JoinReq.RequestStatus.APPROVED) {
            targetRequest.setStatus(JoinReq.RequestStatus.APPROVED); // 상태 업데이트
            familyGroup.addUser(targetRequest.getUser()); // 그룹에 요청 유저 추가
            // joinRequestRepository.delete(targetRequest); // 요청 목록에서 처리 완료된 요청 삭제
        }
        // 요청 거절
        else if (request.getStatus() == JoinReq.RequestStatus.REJECTED) {
            targetRequest.setStatus(JoinReq.RequestStatus.REJECTED);
            // joinRequestRepository.delete(targetRequest); // 요청 목록에서 처리 완료된 요청 삭제
        }
        else {
            throw new InvaildStatusException();
        }
        return ApprovejoinResponse.createWith(targetRequest);
    }

    // 가족명 수정 (O)
    public UpdateGroupNameResponse updateGroupName(Long userId, UpdateGroupNameRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        FamilyGroup familyGroup = user.getFamilyGroup();
        if (familyGroup == null) {
            throw new GroupNotFoundException();
        }
        UpdateGroupNameData updateGroupNameData = UpdateGroupNameData.createWith(request);
        familyGroup.updateName(updateGroupNameData);
        groupRepository.save(familyGroup);
        return UpdateGroupNameResponse.createWith(familyGroup);
    }


}
