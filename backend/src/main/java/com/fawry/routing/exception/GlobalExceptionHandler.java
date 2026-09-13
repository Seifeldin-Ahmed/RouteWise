package com.fawry.routing.exception;

import com.fawry.routing.dto.response.ApiResponse;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse> handleNotFound(ResourceNotFoundException exc) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(exc.getMessage()));
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiResponse> handleEmailExists(EmailAlreadyExistsException exc) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(exc.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse> handleAccessDenied(AccessDeniedException exc) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("You are not allowed to perform this operation"));
    }

    @ExceptionHandler({ValidationException.class, MethodArgumentNotValidException.class,
            HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiResponse> handleBadRequest(Exception exc) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(
                exc instanceof ValidationException ? exc.getMessage() : "Invalid request"));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse> handleConflict(DataAccessException exc) {
        log.warn("Database rejected a write: {}", exc.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(
                exc instanceof DataIntegrityViolationException
                        ? "Other records still depend on this one, so it cannot be changed or removed."
                        : "This request conflicts with another one in progress. Please try again."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleUnexpected(Exception exc) {
        log.error("Unhandled exception", exc);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Something went wrong on our side. Please try again in a moment"));
    }
}
