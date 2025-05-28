package da_ni_ni.backend.emotion.service;

import da_ni_ni.backend.emotion.domain.Emotion;
import da_ni_ni.backend.emotion.dto.*;
import da_ni_ni.backend.emotion.exception.EmotionNotFoundException;
import da_ni_ni.backend.emotion.repository.EmotionRepository;
import da_ni_ni.backend.group.domain.FamilyGroup;
import da_ni_ni.backend.group.dto.UpdateGroupNameData;
import da_ni_ni.backend.group.dto.UpdateGroupNameRequest;
import da_ni_ni.backend.group.dto.UpdateGroupNameResponse;
import da_ni_ni.backend.group.exception.GroupNotFoundException;
import da_ni_ni.backend.group.repository.GroupRepository;
import da_ni_ni.backend.user.domain.User;
import da_ni_ni.backend.user.exception.UserNotFoundException;
import da_ni_ni.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EmotionService {
    private final EmotionRepository emotionRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    // 감정 생성
    public CreateEmotionResponse addEmotion(CreateEmotionRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        // 그룹이 없으면 예외 처리
        if (user.getFamilyGroup() == null) {
            throw new GroupNotFoundException();  // 그룹이 없을 경우 예외 처리
        }

        // 이미 감정을 가진 경우 예외 처리
        if (user.getEmotion() != null) {
            throw new IllegalStateException("이미 감정이 등록되어 있습니다. 수정만 가능합니다.");
        }

        Emotion emotion = Emotion.builder()
                .user(user)
                .emotionType(request.getEmotionType())
                .group(user.getFamilyGroup())
                .build();

        user.setEmotion(emotion);
        emotionRepository.save(emotion);
        return CreateEmotionResponse.createWith(emotion);
    }

    // 감정 수정
    public UpdateEmotionResponse updateEmotionResponse(UpdateEmotionRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        // 감정 정보가 없는 경우 예외 처리
        Emotion userEmotion = user.getEmotion();
        if (userEmotion == null) {
            throw new EmotionNotFoundException();  // 혹은 다른 적절한 예외
        }

        Emotion emotion = emotionRepository.findById(user.getEmotion().getId())
                .orElseThrow(EmotionNotFoundException::new);
        // 본인의 감정만 수정 가능
        if (!emotion.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("본인의 감정만 수정할 수 있습니다.");
        }
        UpdateEmotionData updateEmotionData = UpdateEmotionData.createWith(request);
        emotion.updateType(updateEmotionData);
        if (updateEmotionData.hasNickname()) {
            user.updateNickname(updateEmotionData.getNickName());
        }
        return UpdateEmotionResponse.createWith(emotion);
    }

    // 감정 상세 조회
    public FindEmotionDetailResponse getEmotionDetail(Long emotionId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        Emotion emotion = emotionRepository.findById(emotionId)
                .orElseThrow(EmotionNotFoundException::new);
        if (!emotion.getUser().getFamilyGroup().equals(user.getFamilyGroup())) {
            throw new IllegalArgumentException("같은 그룹 구성원의 감정만 조회할 수 있습니다.");
        }
        return FindEmotionDetailResponse.createWith(emotion);
    }

    // 가족 전체 감정 조회
    public FindTotalEmotionsResponse getGroupEmotions(Long groupId) {
        List<Emotion> emotions = emotionRepository.findAllByUser_FamilyGroupId(groupId);

        List<FindEmotionDetailResponse> emotionList = emotions.stream()
                .map(FindEmotionDetailResponse::createWith)
                .collect(Collectors.toList());

        FamilyGroup groupName = groupRepository.findById(groupId)
                .orElseThrow(GroupNotFoundException::new);

        return FindTotalEmotionsResponse.createWith(groupName, emotionList);
    }

    // 다른 구성원 닉네임 수정
    public UpdateNicknameResponse updateOtherMemberNickname(UpdateNicknameRequest request, Long emotionId, Long userId) {
        Emotion emotion = emotionRepository.findById(emotionId)
                .orElseThrow(EmotionNotFoundException::new);
        String nickName = request.getNickName();
        User targetUser = emotion.getUser();
        User requester = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (!targetUser.getFamilyGroup().equals(requester.getFamilyGroup())) {
            throw new IllegalArgumentException("같은 그룹 구성원만 닉네임을 수정할 수 있습니다.");
        }
        targetUser.updateNickname(nickName);
        return UpdateNicknameResponse.createWith(emotion);
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

