package da_ni_ni.backend.refreshtoken;

import da_ni_ni.backend.user.exception.ExpiredRefreshTokenException;
import da_ni_ni.backend.user.exception.InvalidRefreshTokenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("RefreshToken 예외 클래스 테스트")
class RefreshTokenExceptionTest {

    @Test
    @DisplayName("InvalidRefreshTokenException 생성 및 메시지 확인")
    void createInvalidRefreshTokenException() {
        // given
        String errorMessage = "유효하지 않은 refresh token입니다.";

        // when
        InvalidRefreshTokenException exception = new InvalidRefreshTokenException(errorMessage);

        // then
        assertThat(exception).isInstanceOf(RuntimeException.class);
        assertThat(exception.getMessage()).isEqualTo(errorMessage);
    }

    @Test
    @DisplayName("ExpiredRefreshTokenException 생성 및 메시지 확인")
    void createExpiredRefreshTokenException() {
        // given
        String errorMessage = "Refresh token이 만료되었습니다. 다시 로그인해주세요.";

        // when
        ExpiredRefreshTokenException exception = new ExpiredRefreshTokenException(errorMessage);

        // then
        assertThat(exception).isInstanceOf(RuntimeException.class);
        assertThat(exception.getMessage()).isEqualTo(errorMessage);
    }

    @Test
    @DisplayName("InvalidRefreshTokenException null 메시지 테스트")
    void invalidRefreshTokenExceptionWithNullMessage() {
        // when
        InvalidRefreshTokenException exception = new InvalidRefreshTokenException(null);

        // then
        assertThat(exception.getMessage()).isNull();
    }

    @Test
    @DisplayName("ExpiredRefreshTokenException null 메시지 테스트")
    void expiredRefreshTokenExceptionWithNullMessage() {
        // when
        ExpiredRefreshTokenException exception = new ExpiredRefreshTokenException(null);

        // then
        assertThat(exception.getMessage()).isNull();
    }

    @Test
    @DisplayName("InvalidRefreshTokenException 빈 메시지 테스트")
    void invalidRefreshTokenExceptionWithEmptyMessage() {
        // given
        String emptyMessage = "";

        // when
        InvalidRefreshTokenException exception = new InvalidRefreshTokenException(emptyMessage);

        // then
        assertThat(exception.getMessage()).isEqualTo(emptyMessage);
    }

    @Test
    @DisplayName("ExpiredRefreshTokenException 빈 메시지 테스트")
    void expiredRefreshTokenExceptionWithEmptyMessage() {
        // given
        String emptyMessage = "";

        // when
        ExpiredRefreshTokenException exception = new ExpiredRefreshTokenException(emptyMessage);

        // then
        assertThat(exception.getMessage()).isEqualTo(emptyMessage);
    }

    @Test
    @DisplayName("예외 상속 관계 확인")
    void exceptionInheritanceTest() {
        // given
        InvalidRefreshTokenException invalidException = new InvalidRefreshTokenException("test");
        ExpiredRefreshTokenException expiredException = new ExpiredRefreshTokenException("test");

        // then
        assertThat(invalidException).isInstanceOf(RuntimeException.class);
        assertThat(invalidException).isInstanceOf(Exception.class);
        assertThat(invalidException).isInstanceOf(Throwable.class);

        assertThat(expiredException).isInstanceOf(RuntimeException.class);
        assertThat(expiredException).isInstanceOf(Exception.class);
        assertThat(expiredException).isInstanceOf(Throwable.class);
    }

    @Test
    @DisplayName("예외 던지기 및 잡기 테스트")
    void throwAndCatchExceptionTest() {
        // given
        String invalidMessage = "Invalid token";
        String expiredMessage = "Expired token";

        // when & then - InvalidRefreshTokenException
        assertThatThrownBy(() -> {
            throw new InvalidRefreshTokenException(invalidMessage);
        })
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessage(invalidMessage);

        // when & then - ExpiredRefreshTokenException
        assertThatThrownBy(() -> {
            throw new ExpiredRefreshTokenException(expiredMessage);
        })
                .isInstanceOf(ExpiredRefreshTokenException.class)
                .hasMessage(expiredMessage);
    }

    @Test
    @DisplayName("예외 타입 구분 테스트")
    void exceptionTypeDistinctionTest() {
        // given
        InvalidRefreshTokenException invalidException = new InvalidRefreshTokenException("invalid");
        ExpiredRefreshTokenException expiredException = new ExpiredRefreshTokenException("expired");

        // then
        assertThat(invalidException).isNotInstanceOf(ExpiredRefreshTokenException.class);
        assertThat(expiredException).isNotInstanceOf(InvalidRefreshTokenException.class);

        assertThat(invalidException.getClass()).isNotEqualTo(expiredException.getClass());
    }

    @Test
    @DisplayName("예외 메시지 길이 테스트")
    void exceptionMessageLengthTest() {
        // given
        StringBuilder longMessage = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longMessage.append("Very long error message. ");
        }
        String longMessageStr = longMessage.toString();

        // when
        InvalidRefreshTokenException exception = new InvalidRefreshTokenException(longMessageStr);

        // then
        assertThat(exception.getMessage()).isEqualTo(longMessageStr);
        assertThat(exception.getMessage().length()).isGreaterThan(1000);
    }
}