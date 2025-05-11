package da_ni_ni.backend.daily.exception;

public class LikeNotFoundException extends RuntimeException {

    public LikeNotFoundException() {
        super("좋아요를 찾을 수 없습니다.");
    }
    public LikeNotFoundException(Long dailyId) {
        super(" Daily: " + dailyId + "의 좋아요를 찾을 수 없습니다.");
    }
}

