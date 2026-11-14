package ${packageName}.config;

import com.gallop.fastddd.common.web.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;

/**
 * ${msg.proj.globalExceptionHandler.classDesc}
 * ${msg.proj.globalExceptionHandler.classDetail}
 *
 * @author ${author}
 * @date ${date}
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * ${msg.proj.globalExceptionHandler.validTitle}
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("${msg.proj.globalExceptionHandler.logValidFailed}", message);
        return ApiResponse.error(400, message);
    }

    /**
     * ${msg.proj.globalExceptionHandler.bindTitle}
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("${msg.proj.globalExceptionHandler.logBindFailed}", message);
        return ApiResponse.error(400, message);
    }

    /**
     * ${msg.proj.globalExceptionHandler.constraintTitle}
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        log.warn("${msg.proj.globalExceptionHandler.logConstraintFailed}", message);
        return ApiResponse.error(400, message);
    }

    /**
     * ${msg.proj.globalExceptionHandler.illegalArgTitle}
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("${msg.proj.globalExceptionHandler.logIllegalArg}", e.getMessage());
        return ApiResponse.error(400, e.getMessage());
    }

    /**
     * ${msg.proj.globalExceptionHandler.runtimeTitle}
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleRuntimeException(RuntimeException e) {
        log.error("${msg.proj.globalExceptionHandler.logRuntime}", e);
        // ${msg.proj.globalExceptionHandler.noLeakNote}
        return ApiResponse.error(500, "${msg.proj.globalExceptionHandler.systemError}");
    }

    /**
     * ${msg.proj.globalExceptionHandler.exceptionTitle}
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleException(Exception e) {
        log.error("${msg.proj.globalExceptionHandler.logSystemException}", e);
        return ApiResponse.error(500, "${msg.proj.globalExceptionHandler.systemError}");
    }
}
