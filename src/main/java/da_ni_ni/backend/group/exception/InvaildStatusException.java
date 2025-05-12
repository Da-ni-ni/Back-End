package da_ni_ni.backend.group.exception;


// 요청 처리가 유효하지 않을 때
public class InvaildStatusException  extends RuntimeException {
    public InvaildStatusException() {
        super("알 수 없는 요청 상태입니다.");
    }
}
