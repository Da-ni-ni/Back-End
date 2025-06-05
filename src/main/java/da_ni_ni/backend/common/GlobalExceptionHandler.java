package da_ni_ni.backend.common;

import com.google.firebase.messaging.FirebaseMessagingException;
import da_ni_ni.backend.qna.exception.BadRequestException;
import da_ni_ni.backend.qna.exception.ForbiddenException;
import da_ni_ni.backend.user.dto.ErrorResponseDto;
import da_ni_ni.backend.user.exception.DuplicateEmailException;
import da_ni_ni.backend.user.exception.ExpiredRefreshTokenException;
import da_ni_ni.backend.user.exception.InvalidRefreshTokenException;
import da_ni_ni.backend.user.exception.LoginFailedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponseDto> handleForbidden(ForbiddenException ex) {
        ErrorResponseDto body = new ErrorResponseDto(
                HttpStatus.FORBIDDEN.value(),
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponseDto> handleBadRequest(BadRequestException ex) {
        ErrorResponseDto body = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // 여러 필드 오류 메시지를 모두 수집
        String messages = ex.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("[GlobalExceptionHandler] 유효성 검사 실패: {}", messages);

        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                messages  // "username은 필수입니다; password길이는 8자 이상이어야 합니다."
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponseDto> handleDuplicateEmail(DuplicateEmailException ex) {
        ErrorResponseDto body = new ErrorResponseDto(
                HttpStatus.CONFLICT.value(),
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(LoginFailedException.class)
    public ResponseEntity<ErrorResponseDto> handleLoginFailed(LoginFailedException ex) {
        ErrorResponseDto body = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ErrorResponseDto> handleException(Exception ex) {
//        ErrorResponseDto body = new ErrorResponseDto(
//                HttpStatus.INTERNAL_SERVER_ERROR.value(),
//                "서버 오류가 발생했습니다."
//        );
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
//    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleException(Exception ex) {
        log.error("[GlobalExceptionHandler] 예외 발생", ex);
        ErrorResponseDto body = new ErrorResponseDto(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage() // 디버깅용으로 예외 메시지를 그대로 내려줍니다.
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }


    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidRefreshTokenException(InvalidRefreshTokenException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)  // 401 대신 400으로 변경
                .body(new ErrorResponseDto(400, ex.getMessage()));  // 상태 코드와 메시지 모두 전달
    }

    @ExceptionHandler(ExpiredRefreshTokenException.class)
    public ResponseEntity<ErrorResponseDto> handleExpiredRefreshToken(ExpiredRefreshTokenException e) {
        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),  // 400 코드로 설정
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);  // 400 상태로 반환
    }

    /**
     * firebase 오류
     */
    @ExceptionHandler(com.google.firebase.messaging.FirebaseMessagingException.class)
    public ResponseEntity<ErrorResponseDto> handleFirebaseMessagingException(FirebaseMessagingException ex) {
        // 1) 어떤 토큰/토픽 전송에서 오류가 났는지 로그
        log.error("[GlobalExceptionHandler] FCM 전송 예외 발생: {}", ex.getErrorCode(), ex);

        // 2) 예외 메시지를 그대로 전달하거나, “FCM 전송 실패”로 통일
        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.BAD_GATEWAY.value(),  // FCM과의 통신 오류일 경우 502 Bad Gateway 정도로 써도 무방합니다.
                "푸시 알림 전송 중 오류가 발생했습니다: " + ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error);
    }

    /**
     * db 연결 오류
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponseDto> handleDataAccessException(DataAccessException ex) {
        log.error("[GlobalExceptionHandler] DB 예외 발생", ex);
        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.SERVICE_UNAVAILABLE.value(),  // DB 연결 문제 시 503으로 응답해도 괜찮습니다.
                "데이터베이스 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    //  JSON 바인딩 실패 전용 예외 핸들러 추가
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleBadJson(HttpMessageNotReadableException ex) {
        log.error("[GlobalExceptionHandler] JSON 바인딩 실패", ex);
        ErrorResponseDto body = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                "잘못된 JSON 형식입니다: " + ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
