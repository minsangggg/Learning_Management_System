package com.example.lms.api.error;

import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ApiError> handleApi(ApiException ex) {
    return ResponseEntity.status(mapStatus(ex.getCode()))
        .body(new ApiError(ex.getCode().name(), ex.getMessage()));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiError> handleIllegal(IllegalArgumentException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ApiError(ErrorCode.VALIDATION_ERROR.name(), ex.getMessage()));
  }

  @ExceptionHandler(DataAccessException.class)
  public ResponseEntity<ApiError> handleDb(DataAccessException ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ApiError(ErrorCode.DB_ERROR.name(), "Database error"));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleGeneric(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ApiError(ErrorCode.INTERNAL_ERROR.name(), "Internal server error"));
  }

  private HttpStatus mapStatus(ErrorCode code) {
    return switch (Objects.requireNonNull(code)) {
      case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
      case FORBIDDEN -> HttpStatus.FORBIDDEN;
      case NOT_FOUND -> HttpStatus.NOT_FOUND;
      case VALIDATION_ERROR -> HttpStatus.BAD_REQUEST;
      case CONFLICT -> HttpStatus.CONFLICT;
      case DB_ERROR, EXTERNAL_API_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
      case INTERNAL_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
    };
  }
}
