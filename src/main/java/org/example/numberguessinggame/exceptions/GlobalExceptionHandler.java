package org.example.numberguessinggame.exceptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static Map<String, String> singleError(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        return response;
    }

    @ExceptionHandler(HttpExceptions.BadRequestException.class)
    public ResponseEntity<Map<String, String>> handleBadRequestException(HttpExceptions.BadRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(singleError(ex.getMessage()));
    }

    @ExceptionHandler(HttpExceptions.UnauthorizedException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorizedException(HttpExceptions.UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(singleError(ex.getMessage()));
    }

    @ExceptionHandler(HttpExceptions.ForbiddenException.class)
    public ResponseEntity<Map<String, String>> handleForbiddenException(HttpExceptions.ForbiddenException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(singleError(ex.getMessage()));
    }

    @ExceptionHandler(HttpExceptions.NotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFoundException(HttpExceptions.NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(singleError(ex.getMessage()));
    }

    @ExceptionHandler(HttpExceptions.MethodNotAllowedException.class)
    public ResponseEntity<Map<String, String>> handleMethodNotAllowedException(
            HttpExceptions.MethodNotAllowedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(singleError(ex.getMessage()));
    }

    @ExceptionHandler(HttpExceptions.RequestTimeoutException.class)
    public ResponseEntity<Map<String, String>> handleRequestTimeoutException(
            HttpExceptions.RequestTimeoutException ex) {
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(singleError(ex.getMessage()));
    }

    @ExceptionHandler(HttpExceptions.InternalServerErrorException.class)
    public ResponseEntity<Map<String, String>> handleInternalServerErrorException(
            HttpExceptions.InternalServerErrorException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(singleError(ex.getMessage()));
    }

    @ExceptionHandler(HttpExceptions.ServiceUnavailableException.class)
    public ResponseEntity<Map<String, String>> handleServiceUnavailableException(
            HttpExceptions.ServiceUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(singleError(ex.getMessage()));
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Map<String, String>> handleNullPointerException(NullPointerException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(singleError("Internal server error"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(singleError(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessage> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        ErrorMessage errorMessage = new ErrorMessage("Validation failed", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    }

    /** Maps Spring {@link ResponseStatusException} (used across controllers) to JSON error body. */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatusException(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(singleError(ex.getReason() != null ? ex.getReason() : ex.getStatusCode().toString()));
    }
}
