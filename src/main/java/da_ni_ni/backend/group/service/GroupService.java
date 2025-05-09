package da_ni_ni.backend.group.service;

import da_ni_ni.backend.group.domain.Group;
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
        Group group = Group.create(request.getGroupName(), user);
        group.addUser(user);
        groupRepository.save(group);
        return CreateGroupResponse.createWith(group);
    }

    // 가입 요청
    public JoinGroupResponse requestJoin(Long userId, JoinGroupRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        Group group = groupRepository.findByInviteCode(request.getInviteCode())
                .orElseThrow(InviteCodeNotFoundException::new);
        // 이미 가입이 된 유저 예외 처리
        if (user.getGroup() != null) throw new AlreadyInGroupException();

        // 가입 요청 생성
        JoinReq joinReq = JoinReq.builder()
                .user(user)
                .group(group)
                .inviteCode(request.getInviteCode())
                .status(JoinReq.RequestStatus.PENDING)
                .build();
        joinRequestRepository.save(joinReq);

        return JoinGroupResponse.createWith(joinReq);
    }

/*    // 가입 요청 상태 조회 (본인이 보낸 요청만 확인 가능)
    public GetJoinStatusResponse getMyJoinStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        JoinReq joinReq = joinRequestRepository.findByUser(user)
                .orElseThrow(InvitationRequestNotFoundException::new);
        return GetJoinStatusResponse.createWith(joinReq);
    }*/

    // 가입 요청 목록 조회 (생성자만 확인 가능) (O)
    public GetJoinStatusListResponse getJoinRequestsList(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        Group group = groupRepository.findByAdminUser(user)
                .orElseThrow(GroupNotFoundException::new);

        List<JoinReq> requests = joinRequestRepository.findAllByInviteCode(group.getInviteCode());
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
        Group group = groupRepository.findByAdminUser(user)
                .orElseThrow(GroupNotFoundException::new);
        List<JoinReq> joinReq = joinRequestRepository.findAllByInviteCode(group.getInviteCode());
        if (joinReq.isEmpty()) {
            throw new InvitationRequestNotFoundException();
        }
        JoinReq targetRequest = joinReq.stream()
                .filter(req -> req.getId().equals(request.getRequestId()))
                .findFirst()
                .orElseThrow(InvitationRequestNotFoundException::new);

        // 요청 수락
        if (request.getStatus() == JoinReq.RequestStatus.APPROVED) {
            group.addUser(targetRequest.getUser()); // 그룹에 요청 유저 추가
            joinRequestRepository.delete(targetRequest); // 요청 목록에서 처리 완료된 요청 삭제
        }
        // 요청 거절
        else if (request.getStatus() == JoinReq.RequestStatus.REJECTED) {
            joinRequestRepository.delete(targetRequest); // 요청 목록에서 처리 완료된 요청 삭제
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
        Group group = user.getGroup();
        if (group == null) {
            throw new GroupNotFoundException();
        }
        UpdateGroupNameData updateGroupNameData = UpdateGroupNameData.createWith(request);
        group.updateName(updateGroupNameData);
        groupRepository.save(group);
        return UpdateGroupNameResponse.createWith(group);
    }


}
