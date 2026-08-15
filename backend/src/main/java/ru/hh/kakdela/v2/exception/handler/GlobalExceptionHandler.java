package ru.hh.kakdela.v2.exception.handler;

import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import ru.hh.kakdela.v2.dto.error.ErrorResponse;
import ru.hh.kakdela.v2.exception.ErrorCode;
import ru.hh.kakdela.v2.exception.Kd2Exception;
import ru.hh.kakdela.v2.exception.Kd2OblectRelatedException;
import ru.hh.kakdela.v2.exception.ResetCodeException;
import ru.hh.kakdela.v2.mapper.ErrorMapper;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(Kd2OblectRelatedException.class)
  public ResponseEntity<ErrorResponse> handleKd2OblectRelatedException(
      Kd2OblectRelatedException ex, WebRequest request
  ) {
    UUID id = UUID.randomUUID();
    return ResponseEntity
        .status(ex.getHttpStatus())
        .body(ErrorMapper.getErrorResponse(id, ex, request));
  }

  @ExceptionHandler(Kd2Exception.class)
  public ResponseEntity<ErrorResponse> handleKd2Exception(
      Kd2Exception ex, WebRequest request
  ) {
    UUID id = UUID.randomUUID();
    return ResponseEntity
        .status(ex.getHttpStatus())
        .body(ErrorMapper.getErrorResponse(id, ex, request));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public Map<String, String> handleValidationExceptions(
      MethodArgumentNotValidException ex
  ) {
    UUID id = UUID.randomUUID();
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach((error) -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });

    logErrorMessage("Нарушение ограничений в данных", id, ex.getMessage());
    return errors;
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolation(
      ConstraintViolationException ex, WebRequest request
  ) {
    UUID id = UUID.randomUUID();
    logErrorMessage("Нарушение ограничений в данных", id, ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ErrorMapper.getErrorResponse(
            id, ErrorCode.BAD_REQUEST_DATA, ex.getMessage(), request));
  }

  @ExceptionHandler(jakarta.persistence.EntityNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleEntityNotFound(
      jakarta.persistence.EntityNotFoundException ex, WebRequest request
  ) {
    UUID id = UUID.randomUUID();
    logErrorMessage("Сущность не найдена", id, ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(ErrorMapper.getErrorResponse(
            id, ErrorCode.ENTITY_NOT_FOUND, ex.getMessage(), request));
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleBadCredentials(
      BadCredentialsException ex, WebRequest request
  ) {
    UUID id = UUID.randomUUID();
    logError("Неправильный логин или пароль", id, ex);
    return ResponseEntity
        .status(HttpStatus.UNAUTHORIZED)
        .body(ErrorMapper.getErrorResponse(
            id, ErrorCode.BAD_CREDENTIALS, "Неправильный логин или пароль", request));
  }

  @ExceptionHandler(UsernameNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleUsernameNotFound(
      UsernameNotFoundException ex, WebRequest request
  ) {
    UUID id = UUID.randomUUID();
    logError("Аккаунт не найден", id, ex);
    return ResponseEntity
        .status(HttpStatus.UNAUTHORIZED)
        .body(ErrorMapper.getErrorResponse(
            id, ErrorCode.BAD_CREDENTIALS, "Неправильный логин или пароль", request));
  }

  @ExceptionHandler(DisabledException.class)
  public ResponseEntity<ErrorResponse> handleDisabled(
      DisabledException ex, WebRequest request
  ) {
    UUID id = UUID.randomUUID();
    logError("Аккаунт удалён", id, ex);
    return ResponseEntity
        .status(HttpStatus.UNAUTHORIZED)
        .body(ErrorMapper.getErrorResponse(
            id, ErrorCode.ACCOUNT_DELETED, "Аккаунт удалён", request));
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoResourceFound(
      NoResourceFoundException ex, WebRequest request
  ) {
    UUID id = UUID.randomUUID();
    logError("Ресурс не найден", id, ex);
    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(ErrorMapper.getErrorResponse(
            id, ErrorCode.RESOURCE_NOT_FOUND, "Ресурс не найден", request));
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ErrorResponse> handleResponseStatusException(
      ResponseStatusException ex, WebRequest request
  ) {
    UUID id = UUID.randomUUID();
    logError(ex.getReason(), id, ex);
    return ResponseEntity
        .status(ex.getStatusCode())
        .body(ErrorMapper.getErrorResponse(
            id, ErrorCode.UNDEFINED, ex.getMessage(), request));
  }

  @ExceptionHandler(ResetCodeException.class)
  public ResponseEntity<ErrorResponse> handleResetCodeException(
      ResetCodeException ex, WebRequest request
  ) {
    UUID id = UUID.randomUUID();
    logError(ex.getReason(), id, ex);
    return ResponseEntity
        .status(HttpStatus.TOO_MANY_REQUESTS)
        .body(ErrorMapper.getErrorResponse(
            id, ErrorCode.UNDEFINED, "Осталось попыток: " + ex.getRemainingAttempts(), request));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleAllExceptions(
      Exception ex, WebRequest request
  ) {
    UUID id = UUID.randomUUID();
    logError("Неожиданная ошибка", id, ex);
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ErrorMapper.getErrorResponse(
            id, ErrorCode.INTERNAL_SERVER, "Неожиданная внутренняя ошибка", request));
  }

  private void logError(String message, UUID id, Exception ex) {
    final String logMessage = "%s (errorId=%s): ".formatted(message, id);
    log.error(logMessage, ex);
  }

  private void logErrorMessage(String message, UUID id, String details) {
    final String logMessage = "%s (errorId=%s): %s".formatted(message, id, details);
    log.error(logMessage);
  }
}
