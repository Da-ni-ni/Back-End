package da_ni_ni.backend.common;

import da_ni_ni.backend.qna.exception.BadRequestException;
import da_ni_ni.backend.qna.exception.ForbiddenException;
import da_ni_ni.backend.user.dto.ErrorResponseDto;
import da_ni_ni.backend.user.exception.DuplicateEmailException;
import da_ni_ni.backend.user.exception.ExpiredRefreshTokenException;
import da_ni_ni.backend.user.exception.InvalidRefreshTokenException;
import da_ni_ni.backend.user.exception.LoginFailedException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
        // 가장 첫 번째 검증 오류 메시지를 꺼내서 응답에 담습니다.
        String errorMessage = ex.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .findFirst()
                .orElse("잘못된 요청입니다.");
        ErrorResponseDto body = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                errorMessage
        );
        return ResponseEntity.badRequest().body(body);
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleException(Exception ex) {
        ErrorResponseDto body = new ErrorResponseDto(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "서버 오류가 발생했습니다."
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

}
