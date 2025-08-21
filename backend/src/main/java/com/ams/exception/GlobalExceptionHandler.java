package com.ams.exception;

import com.ams.dto.ApiResponses;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Custom business exceptions
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponses<Object>> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        logger.warn("Resource not found: {} - URI: {}", ex.getMessage(), request.getDescription(false));
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponses.error(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponses<Object>> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        logger.warn("Illegal argument: {} - URI: {}", ex.getMessage(), request.getDescription(false));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponses.error(ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponses<Object>> handleIllegalStateException(IllegalStateException ex, WebRequest request) {
        logger.warn("Illegal state: {} - URI: {}", ex.getMessage(), request.getDescription(false));
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponses.error(ex.getMessage()));
    }

    // Validation exceptions
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponses<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        String errorMessage = "入力値に誤りがあります: " + 
                errors.entrySet().stream()
                        .map(entry -> entry.getKey() + "=" + entry.getValue())
                        .collect(Collectors.joining(", "));

        logger.warn("Validation failed: {}", errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponses.error(errorMessage));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponses<Object>> handleConstraintViolationException(ConstraintViolationException ex) {
        String errorMessage = "制約違反: " + 
                ex.getConstraintViolations().stream()
                        .map(ConstraintViolation::getMessage)
                        .collect(Collectors.joining(", "));

        logger.warn("Constraint violation: {}", errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponses.error(errorMessage));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponses<Object>> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String errorMessage = String.format("パラメータ '%s' の値 '%s' が不正です", ex.getName(), ex.getValue());
        logger.warn("Type mismatch: {}", errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponses.error(errorMessage));
    }

    // Security exceptions
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponses<Object>> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        logger.warn("Authentication failed: {} - URI: {}", ex.getMessage(), request.getDescription(false));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponses.error("認証に失敗しました"));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponses<Object>> handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {
        logger.warn("Bad credentials: {} - URI: {}", ex.getMessage(), request.getDescription(false));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponses.error("認証情報が正しくありません"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponses<Object>> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        logger.warn("Access denied: {} - URI: {}", ex.getMessage(), request.getDescription(false));
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponses.error("アクセス権限がありません"));
    }

    // Database exceptions
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponses<Object>> handleDataIntegrityViolationException(
            org.springframework.dao.DataIntegrityViolationException ex, WebRequest request) {
        logger.error("Data integrity violation: {} - URI: {}", ex.getMessage(), request.getDescription(false));
        
        String userMessage = "データの整合性エラーが発生しました";
        
        // Check for common constraint violations
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("unique")) {
                userMessage = "既に存在するデータです";
            } else if (ex.getMessage().contains("foreign key")) {
                userMessage = "関連するデータが存在しません";
            } else if (ex.getMessage().contains("not null")) {
                userMessage = "必須項目が入力されていません";
            }
        }
        
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponses.error(userMessage));
    }

    @ExceptionHandler(org.springframework.dao.DataAccessException.class)
    public ResponseEntity<ApiResponses<Object>> handleDataAccessException(
            org.springframework.dao.DataAccessException ex, WebRequest request) {
        logger.error("Database access error: {} - URI: {}", ex.getMessage(), request.getDescription(false));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponses.error("データベースアクセスエラーが発生しました"));
    }

    // Transaction exceptions
    @ExceptionHandler(org.springframework.transaction.TransactionException.class)
    public ResponseEntity<ApiResponses<Object>> handleTransactionException(
            org.springframework.transaction.TransactionException ex, WebRequest request) {
        logger.error("Transaction error: {} - URI: {}", ex.getMessage(), request.getDescription(false));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponses.error("トランザクションエラーが発生しました"));
    }

    // HTTP method not supported
    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponses<Object>> handleHttpRequestMethodNotSupportedException(
            org.springframework.web.HttpRequestMethodNotSupportedException ex) {
        logger.warn("HTTP method not supported: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponses.error("サポートされていないHTTPメソッドです"));
    }

    // Missing request parameter
    @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponses<Object>> handleMissingServletRequestParameterException(
            org.springframework.web.bind.MissingServletRequestParameterException ex) {
        String errorMessage = String.format("必須パラメータ '%s' が指定されていません", ex.getParameterName());
        logger.warn("Missing parameter: {}", errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponses.error(errorMessage));
    }

    // General runtime exceptions
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponses<Object>> handleRuntimeException(RuntimeException ex, WebRequest request) {
        logger.error("Runtime exception: {} - URI: {}", ex.getMessage(), request.getDescription(false), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponses.error("システムエラーが発生しました"));
    }

    // Generic exception handler (fallback)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponses<Object>> handleGlobalException(Exception ex, WebRequest request) {
        logger.error("Unexpected error: {} - URI: {}", ex.getMessage(), request.getDescription(false), ex);
        
        // Create detailed error response for debugging (only in development)
        ApiResponses<Object> response = ApiResponses.error("システムエラーが発生しました");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    // Custom exception for business rule violations
    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ApiResponses<Object>> handleBusinessRuleViolationException(
            BusinessRuleViolationException ex, WebRequest request) {
        logger.warn("Business rule violation: {} - URI: {}", ex.getMessage(), request.getDescription(false));
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponses.error(ex.getMessage()));
    }

    // Helper method to create error details
    private Map<String, Object> createErrorDetails(String error, String path, HttpStatus status) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", status.value());
        errorDetails.put("error", status.getReasonPhrase());
        errorDetails.put("message", error);
        errorDetails.put("path", path);
        return errorDetails;
    }
}