package exception;

import com.codeperfection.shipit.exception.GlobalExceptionHandler;
import com.codeperfection.shipit.exception.InternalServerErrorException;
import com.codeperfection.shipit.exception.clienterror.EntityNotFoundException;
import com.codeperfection.shipit.exception.errordto.ErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.lang.annotation.Annotation;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@SuppressWarnings({"ConstantConditions", "ThrowableNotThrown"})
@ExtendWith(MockitoExtension.class)
public class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    public void clientErrorExceptionReturnsExpectedResponseEntity() {
        final var exception = new EntityNotFoundException(UUID.randomUUID());

        final var response = globalExceptionHandler.clientErrorException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.valueOf(exception.getStatus()));

        final var responseBody = response.getBody();
        assertThat(responseBody.getErrorType()).isEqualTo(exception.getErrorType());
        assertThat(responseBody.getStatus()).isEqualTo(exception.getStatus());
        assertThat(responseBody.getMessage()).isEqualTo(exception.getMessage());
        final var epsilon = within(10, ChronoUnit.SECONDS);
        assertThat(responseBody.getTimestamp()).isCloseToUtcNow(epsilon);
    }

    @Test
    public void httpMessageNotReadableExceptionReturnsExpectedResponseEntity() {
        final var exception = new HttpMessageNotReadableException("Some message", mock(HttpInputMessage.class));

        final var response = globalExceptionHandler.httpMessageNotReadableException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        final var responseBody = response.getBody();
        assertThat(responseBody.getErrorType()).isEqualTo(ErrorType.INVALID_REQUEST);
        assertThat(responseBody.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(responseBody.getMessage()).isEqualTo(exception.getMessage());
        final var epsilon = within(10, ChronoUnit.SECONDS);
        assertThat(responseBody.getTimestamp()).isCloseToUtcNow(epsilon);
    }

    @Test
    public void methodArgumentTypeMismatchExceptionIfNotPathVariableForwardsException() {
        final var exception = mock(MethodArgumentTypeMismatchException.class);
        final var methodParameter = mock(MethodParameter.class);
        doReturn(methodParameter).when(exception).getParameter();
        doReturn(null).when(methodParameter).getParameterAnnotation(PathVariable.class);

        assertThatExceptionOfType(MethodArgumentTypeMismatchException.class).isThrownBy(() ->
                globalExceptionHandler.methodArgumentTypeMismatchException(exception));
    }

    @Test
    public void methodArgumentTypeMismatchExceptionIfPathVariableReturnsExpectedResponseEntity() {
        final var exception = mock(MethodArgumentTypeMismatchException.class);
        final var methodParameter = mock(MethodParameter.class);
        doReturn(methodParameter).when(exception).getParameter();
        doReturn(mock(Annotation.class)).when(methodParameter).getParameterAnnotation(PathVariable.class);
        doReturn("paramName").when(exception).getName();
        doReturn("some message").when(exception).getMessage();

        final var response = globalExceptionHandler.methodArgumentTypeMismatchException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        final var responseBody = response.getBody();
        assertThat(responseBody.getErrorType()).isEqualTo(ErrorType.INVALID_PATH_VARIABLE);
        assertThat(responseBody.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(responseBody.getMessage()).contains(exception.getMessage());
        final var epsilon = within(10, ChronoUnit.SECONDS);
        assertThat(responseBody.getTimestamp()).isCloseToUtcNow(epsilon);
    }

    @Test
    public void internalServerErrorExceptionReturnsExpectedResponseEntity() {
        final var exception = new InternalServerErrorException("Some message");
        final var response = globalExceptionHandler.internalServerErrorException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        final var responseBody = response.getBody();
        assertThat(responseBody.getErrorType()).isEqualTo(ErrorType.INTERNAL_SERVER_ERROR);
        assertThat(responseBody.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(responseBody.getMessage()).contains(exception.getMessage());
        final var epsilon = within(10, ChronoUnit.SECONDS);
        assertThat(responseBody.getTimestamp()).isCloseToUtcNow(epsilon);

    }

}
