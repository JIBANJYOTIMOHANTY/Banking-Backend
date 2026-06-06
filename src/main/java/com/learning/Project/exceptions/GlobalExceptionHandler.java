package com.learning.Project.exceptions;

import com.learning.Project.dto.ApiResponse;
import com.learning.Project.constants.MessageConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BankAccountExceptions.class)
    public ResponseEntity<ApiResponse<Object>> handleBankAccountExceptions(BankAccountExceptions ex) {
        ApiResponse<Object> response = new ApiResponse<>(1, ex.getMessage(), List.of());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<ApiResponse<Object>> handleRateLimitException(RateLimitException ex) {
        ApiResponse<Object> response = new ApiResponse<>(1, ex.getMessage(), List.of());
        return new ResponseEntity<>(response, HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneralExceptions(Exception ex) {
        ApiResponse<Object> response = new ApiResponse<>(1, MessageConstants.UNEXPECTED_ERROR + ex.getMessage(),
                List.of());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
