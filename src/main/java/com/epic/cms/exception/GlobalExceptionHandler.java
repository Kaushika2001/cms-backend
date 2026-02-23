package com.epic.cms.exception;

import com.epic.cms.util.LoggerUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request) {
        
        log.error("Resource not found: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResourceException(
            DuplicateResourceException ex,
            HttpServletRequest request) {
        
        log.error("Duplicate resource: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequestException(
            InvalidRequestException ex,
            HttpServletRequest request) {
        
        log.error("Invalid request: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        
        List<String> details = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            details.add(error.getField() + ": " + error.getDefaultMessage());
        }
        
        // Log concise error message instead of full stack trace
        log.error("Validation failed on {}: {} field(s) - {}", 
                request.getRequestURI(), 
                details.size(),
                String.join(", ", details));
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Input validation failed")
                .path(request.getRequestURI())
                .details(details)
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request) {
        
        log.error("Access denied: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .message("Access denied")
                .path(request.getRequestURI())
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        
        log.error("Invalid request body: {}", ex.getMessage());
        
        String message = "Invalid request body";
        
        // Extract more specific error message for common issues
        if (ex.getCause() != null) {
            String causeMessage = ex.getCause().getMessage();
            
            // Handle date parsing errors
            if (causeMessage != null && causeMessage.contains("LocalDate")) {
                if (causeMessage.contains("Invalid date")) {
                    // Extract the invalid date from the error message
                    if (causeMessage.contains("'NOVEMBER 31'")) {
                        message = "Invalid date: November only has 30 days";
                    } else if (causeMessage.contains("'FEBRUARY 30'") || causeMessage.contains("'FEBRUARY 31'")) {
                        message = "Invalid date: February only has 28 or 29 days";
                    } else if (causeMessage.contains("'APRIL 31'")) {
                        message = "Invalid date: April only has 30 days";
                    } else if (causeMessage.contains("'JUNE 31'")) {
                        message = "Invalid date: June only has 30 days";
                    } else if (causeMessage.contains("'SEPTEMBER 31'")) {
                        message = "Invalid date: September only has 30 days";
                    } else {
                        message = "Invalid date format. Please use a valid date (YYYY-MM-DD)";
                    }
                } else if (causeMessage.contains("could not be parsed")) {
                    message = "Invalid date format. Expected format: YYYY-MM-DD (e.g., 2027-11-30)";
                }
            } else if (causeMessage != null && causeMessage.contains("Cannot deserialize")) {
                message = "Invalid JSON format: " + causeMessage.substring(0, Math.min(causeMessage.length(), 200));
            }
        }
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(message)
                .path(request.getRequestURI())
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request) {
        
        log.error("Illegal argument: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex,
            HttpServletRequest request) {
        
        // Check if this is an encryption/decryption related error
        if (isEncryptionError(ex)) {
            LoggerUtil.error(GlobalExceptionHandler.class, "Encryption/Decryption error occurred", ex);
            
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .error("Encryption Error")
                    .message("Failed to process encrypted data. Please verify the payload format and encryption key.")
                    .path(request.getRequestURI())
                    .build();
            
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
        
        // Handle general runtime exceptions
        LoggerUtil.error(GlobalExceptionHandler.class, "Unexpected error occurred", ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("An unexpected error occurred. Please contact support if the issue persists.")
                .path(request.getRequestURI())
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Check if the exception is related to encryption/decryption.
     * 
     * @param ex The exception to check
     * @return true if encryption-related, false otherwise
     */
    private boolean isEncryptionError(Exception ex) {
        if (ex == null) {
            return false;
        }
        
        String message = ex.getMessage();
        if (message == null) {
            return false;
        }
        
        // Check for common encryption error patterns
        return message.contains("Encryption failed") ||
               message.contains("Decryption failed") ||
               message.contains("Invalid encrypted data format") ||
               message.contains("encryption key") ||
               message.contains("AES") ||
               message.contains("GCM") ||
               (ex.getCause() != null && 
                (ex.getCause().getClass().getName().contains("crypto") ||
                 ex.getCause().getClass().getName().contains("Cipher")));
    }
}
