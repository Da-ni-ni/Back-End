package da_ni_ni.backend.daily.exception;

public class DailyNotFoundException extends RuntimeException {
    public DailyNotFoundException() {
        super("게시글을 찾을 수 없습니다.");
    }

    public DailyNotFoundException(Long dailyId) {
        super("Daily: " + dailyId + "를 찾을 수 없습니다.");
    }
}

