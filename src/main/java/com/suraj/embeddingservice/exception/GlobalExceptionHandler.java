package com.suraj.embeddingservice.exception;

import com.suraj.embeddingservice.common.HeaderNames;
import com.suraj.embeddingservice.common.MdcKeys;
import com.suraj.embeddingservice.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(this::formatFieldError)
                .orElse(ErrorMessage.REQUEST_VALIDATION_FAILED);
        return build(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, message, request);
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            IllegalArgumentException.class
    })
    ResponseEntity<ErrorResponse> handleBadRequest(Exception exception, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, exception.getMessage(), request);
    }

    @ExceptionHandler(EmbeddingException.class)
    ResponseEntity<ErrorResponse> handleEmbedding(EmbeddingException exception, HttpServletRequest request) {
        log.warn("Handled embedding exception: code={}, path={}, message={}",
                exception.errorCode(), request.getRequestURI(), exception.getMessage());
        return build(exception.httpStatus(), exception.errorCode(), exception.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> handleUnknown(Exception exception, HttpServletRequest request) {
        log.error("Unhandled service exception: path={}", request.getRequestURI(), exception);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR,
                ErrorMessage.UNEXPECTED_SERVICE_FAILURE, request);
    }

    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + " " + fieldError.getDefaultMessage();
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, ErrorCode error, String message, HttpServletRequest request) {
        String correlationId = resolveCorrelationId(request);
        return ResponseEntity.status(status).body(new ErrorResponse(
                Instant.now(),
                status.value(),
                error.name(),
                message,
                request.getRequestURI(),
                correlationId
        ));
    }

    private String resolveCorrelationId(HttpServletRequest request) {
        String correlationId = MDC.get(MdcKeys.CORRELATION_ID);
        return correlationId == null || correlationId.isBlank()
                ? request.getHeader(HeaderNames.CORRELATION_ID)
                : correlationId;
    }
}
