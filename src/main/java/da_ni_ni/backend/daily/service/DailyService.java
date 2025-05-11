package da_ni_ni.backend.daily.service;

import da_ni_ni.backend.daily.domain.Comment;
import da_ni_ni.backend.daily.domain.Daily;
import da_ni_ni.backend.daily.domain.DailyLike;
import da_ni_ni.backend.daily.dto.*;
import da_ni_ni.backend.daily.exception.CommentNotFoundException;
import da_ni_ni.backend.daily.exception.DailyNotFoundException;
import da_ni_ni.backend.daily.exception.LikeNotFoundException;
import da_ni_ni.backend.daily.repository.CommentRepository;
import da_ni_ni.backend.daily.repository.DailyRepository;
import da_ni_ni.backend.daily.repository.LikeRepository;
import da_ni_ni.backend.group.exception.GroupNotFoundException;
import da_ni_ni.backend.user.domain.User;
import da_ni_ni.backend.user.exception.UserNotFoundException;
import da_ni_ni.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class DailyService {
    private final DailyRepository dailyRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;


    // daily_id로 게시글 조회
    public Daily findById(final Long dailyId) {
        return dailyRepository.findById(dailyId)
                .orElseThrow(DailyNotFoundException::new);
    }

    // comment_id로 댓글 조회
    public Comment findCommentByCommentId(final Long commentId) {
        return commentRepository.findCommentByCommentId(commentId)
                .orElseThrow(CommentNotFoundException::new);
    }

    // 게시글로 댓글 조회
    public List<Comment> findCommentAllByDailyId(Long dailyId) {
        Daily daily = findById(dailyId);
        List<Comment> dailyComments = commentRepository.findAllByDaily(daily);
        if (dailyComments.isEmpty()) {
            throw new CommentNotFoundException(dailyId);
        }
        return dailyComments;
    }

    // 멤버로 댓글 조회
    public List<Comment> findCommentByMember(final User user) {
        List<Comment> memberComments = commentRepository.findByUser(user);
        if (memberComments.isEmpty()) {
            throw new CommentNotFoundException(user.getId());
        }
        return memberComments;
    }

    // 게시글로 좋아요 조회
    public List<DailyLike> findLikeAllByDailyId(final Long dailyId) {
        Daily daily = findById(dailyId);
        List<DailyLike> dailyDailyLikes = likeRepository.findAllByDaily(daily);
        if (dailyDailyLikes.isEmpty()) {
            throw new LikeNotFoundException(dailyId);
        }
        return dailyDailyLikes;
    }


    // 게시글 추가
    public CreateDailyResponse addDaily(CreateDailyRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        // 그룹이 없으면 예외 처리
        if (user.getFamilyGroup() == null) {
            throw new GroupNotFoundException();  // 그룹이 없을 경우 예외 처리
        }
        Daily daily = Daily.builder()
                .date(LocalDate.now())
                .user(user)
                .content(request.getContent())
                .build();
        dailyRepository.save(daily);
        return CreateDailyResponse.createWith(daily);
    }

    // 게시글 수정
    public UpdateDailyResponse updateDaily(UpdateDailyRequest request, Long dailyId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        Daily daily = dailyRepository.findById(dailyId)
                .orElseThrow(DailyNotFoundException::new);
        if (!daily.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("본인의 게시글만 수정할 수 있습니다.");
        }
        UpdateDailyData updateDailyData = UpdateDailyData.createWith(request);
        daily.updateDaily(updateDailyData);
        dailyRepository.save(daily);
        return UpdateDailyResponse.createWith(daily);
    }

    // 게시글 삭제
    public DeleteDailyResponse deleteDaily(Long dailyId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        Daily daily = dailyRepository.findById(dailyId)
                .orElseThrow(DailyNotFoundException::new);
        if (!daily.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("본인의 게시글만 삭제할 수 있습니다.");
        }
        // 게시글에 달린 댓글 삭제
        List<Comment> comments = findCommentAllByDailyId(dailyId);
        commentRepository.deleteAll(comments);

        // 게시글에 달린 좋아요 삭제
        List<DailyLike> dailyLikes = findLikeAllByDailyId(dailyId);
        likeRepository.deleteAll(dailyLikes);

        // 게시글 삭제
        dailyRepository.delete(daily);
        return DeleteDailyResponse.createWith(daily);
    }

    // 댓글 추가
    public CreateCommentResponse addComment(CreateCommentRequest request, Long dailyId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        Daily daily = dailyRepository.findById(dailyId)
                .orElseThrow(DailyNotFoundException::new);
        Comment comment = Comment.builder()
                .user(user)
                .content(request.getContent())
                .build();
        commentRepository.save(comment);
        // 댓글 + 1
        daily.setCommentCount(daily.getCommentCount() + 1);
        return CreateCommentResponse.createWith(comment);
    }

    // 댓글 수정
    public UpdateCommentResponse updateComment(UpdateCommentRequest request, Long dailyId, Long commentId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        Daily daily = dailyRepository.findById(dailyId)
                .orElseThrow(DailyNotFoundException::new);
        Comment comment = findCommentByCommentId(commentId);
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("본인의 댓글만 수정할 수 있습니다.");
        }
        UpdateCommentData updateCommentData = UpdateCommentData.createWith(request);
        comment.updateComment(updateCommentData);
        commentRepository.save(comment);

        return UpdateCommentResponse.createWith(comment);
    }

    // 댓글 삭제
    public DeleteCommentResponse deleteComment(Long dailyId, Long commentId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        Daily daily = dailyRepository.findById(dailyId)
                .orElseThrow(DailyNotFoundException::new);
        Comment comment = findCommentByCommentId(commentId);

        // 1. 댓글이 해당 게시글에 속해 있는지 확인
        if (!comment.getDaily().getId().equals(daily.getId())) {
            throw new IllegalArgumentException("댓글이 해당 게시글에 속하지 않습니다.");
        }

        // 2. 댓글 작성자 본인인지 확인
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("본인의 댓글만 삭제할 수 있습니다.");
        }

        // 댓글 - 1
        daily.setCommentCount(daily.getCommentCount() - 1);
        commentRepository.delete(comment);
        return DeleteCommentResponse.createWith(comment);
    }

    // 좋아요 토글
    public ToggleLikeResponse toggleLike(Long dailyId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        Daily daily = findById(dailyId);

        Optional<DailyLike> existingLike = likeRepository.findByDailyAndUser(daily, user);

        // 좋아요가 이미 존재하면
        if (existingLike.isPresent()) {
            DailyLike dailyLike = existingLike.get();
            // 내가 누른 좋아요일 경우 -> 좋아요 삭제
            if (dailyLike.getUser().getId().equals(userId)) {
                likeRepository.delete(dailyLike);
                // 좋아요 - 1
                daily.setLikeCount(daily.getLikeCount() - 1);
                return new ToggleLikeResponse(false);
            }
            // 다른 사람이 누른 좋아요일 경우 -> 좋아요 등록
            else {
                DailyLike myDailyLike = DailyLike.builder()
                        .daily(daily)
                        .user(user)
                        .build();
                likeRepository.save(myDailyLike);
                // 좋아요 + 1
                daily.setLikeCount(daily.getLikeCount() + 1);
                return new ToggleLikeResponse(true);
            }

        } else {
            // 좋아요가 존재하지 않을 경우 -> 좋아요 등록
            DailyLike newDailyLike = DailyLike.builder()
                    .daily(daily)
                    .user((user))
                    .build();
            likeRepository.save(newDailyLike);
            // 좋아요 + 1
            daily.setLikeCount(daily.getLikeCount() + 1);
            return new ToggleLikeResponse(true);
        }
    }

    // 게시글 상세 조회
    @Transactional(readOnly = true)
    public FindDailyDetailResponse getDailyDetail(Long dailyId) {
        Daily daily = findById(dailyId);
        List<Comment> comments = commentRepository.findAllByDaily(daily);
        List<String> commentContents = comments.stream()
                .map(comment -> "[" + comment.getUser().getNickName() + "]" + comment.getContent())
                .collect(Collectors.toList());
        return FindDailyDetailResponse.createWith(daily, commentContents);
    }

    // 주간 게시글 조회
    @Transactional(readOnly = true)
    public FindWeekDailyResponse getWeeklyDailies (Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.with(DayOfWeek.MONDAY);
        LocalDate endDate = startDate.plusDays(6);

        List<Daily> dailies = dailyRepository.findAllByGroupAndDateBetween(user.getFamilyGroup(), startDate, endDate);

        List<DailySimpleData> dailyList = dailies.stream()
                .map(daily -> {
                    long likeCount = likeRepository.countByDaily(daily);
                    long commentCount = commentRepository.countByDaily(daily);
                    return DailySimpleData.createWith(daily, likeCount, commentCount);
                })
                .collect(Collectors.toList());

        return FindWeekDailyResponse.createWith(dailyList);
    }

}
