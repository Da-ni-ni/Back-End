package da_ni_ni.backend.common;

import da_ni_ni.backend.qna.exception.ForbiddenException;
import da_ni_ni.backend.qna.exception.BadRequestException;
import da_ni_ni.backend.user.dto.ErrorResponseDto;
import da_ni_ni.backend.user.exception.DuplicateEmailException;
import da_ni_ni.backend.user.exception.LoginFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
}
