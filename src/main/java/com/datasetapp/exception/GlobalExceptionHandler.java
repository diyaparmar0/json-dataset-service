package com.datasetapp.exception;

import com.datasetapp.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DatasetNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(DatasetNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(DuplicateRecordException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateRecordException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler({InvalidRecordException.class, InvalidQueryException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, "Request body is missing or is not valid JSON");
    }

    // Spring's own web errors (wrong Content-Type -> 415, wrong method -> 405,
    // unknown URL -> 404, ...) carry their own status. Keep that status instead of a 500.
    @ExceptionHandler({
            org.springframework.web.HttpMediaTypeNotSupportedException.class,
            org.springframework.web.HttpRequestMethodNotSupportedException.class,
            org.springframework.web.servlet.resource.NoResourceFoundException.class,
            org.springframework.web.bind.MissingServletRequestParameterException.class,
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> handleSpringWebException(Exception ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (ex instanceof org.springframework.web.HttpMediaTypeNotSupportedException) {
            status = HttpStatus.UNSUPPORTED_MEDIA_TYPE;
        } else if (ex instanceof org.springframework.web.HttpRequestMethodNotSupportedException) {
            status = HttpStatus.METHOD_NOT_ALLOWED;
        } else if (ex instanceof org.springframework.web.servlet.resource.NoResourceFoundException) {
            status = HttpStatus.NOT_FOUND;
        }
        return build(status, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class).error("Unexpected error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong on the server");
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        ErrorResponse body = new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), message);
        return ResponseEntity.status(status).body(body);
    }
}