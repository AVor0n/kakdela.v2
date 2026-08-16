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
import ru.hh.kakdela.v2.exception.Kd2AuthenticationException;
import ru.hh.kakdela.v2.exception.Kd2Exception;
import ru.hh.kakdela.v2.exception.Kd2ObjectRelatedException;
import ru.hh.kakdela.v2.exception.ResetCodeException;
import ru.hh.kakdela.v2.mapper.ErrorMapper;
import ru.hh.kakdela.v2.security.CustomDisabledException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final String AUTHENTICATION_ERROR_MESSAGE = "Ошибка аутентификации";
  private static final String WRONG_LOGIN_OR_PASSWORD_MESSAGE = "Неверный логин или пароль";

  @ExceptionHandler(Kd2ObjectRelatedException.class)
  public ResponseEntity<ErrorResponse> handleKd2ObjectRelatedException(
      Kd2ObjectRelatedException ex, WebRequest request
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

  @ExceptionHandler(Kd2AuthenticationException.class)
  public ResponseEntity<ErrorResponse> handleKd2AuthenticationException(
      Kd2AuthenticationException ex, WebRequest request
  ) {
    UUID id = UUID.randomUUID();
    return ResponseEntity
        .status(HttpStatus.UNAUTHORIZED)
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

    logError("Нарушение ограничений в данных", id, ex);
    return errors;
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolation(
      ConstraintViolationException ex, WebRequest request
  ) {
    UUID id = UUID.randomUUID();
    logError("Нарушение ограничений в данных", id, ex);
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
    logError("Сущность не найдена", id, ex);
    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(ErrorMapper.getErrorResponse(
            id, ErrorCode.ENTITY_NOT_FOUND, ex.getMessage(), request));
  }

  @ExceptionHandler(UsernameNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleUsernameNotFound(
      UsernameNotFoundException ex, WebRequest request
  ) {
    UUID id = UUID.randomUUID();

    if (ex.getName() != null) {
      logError(AUTHENTICATION_ERROR_MESSAGE, id, ex.getName(), ex);
    } else {
      logError(AUTHENTICATION_ERROR_MESSAGE, id, ex);
    }

    return ResponseEntity
        .status(HttpStatus.UNAUTHORIZED)
        .body(ErrorMapper.getErrorResponse(
            id, ErrorCode.BAD_CREDENTIALS, WRONG_LOGIN_OR_PASSWORD_MESSAGE, request));
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleBadCredentials(
      BadCredentialsException ex, WebRequest request
  ) {
    UUID id = UUID.randomUUID();
    logError(AUTHENTICATION_ERROR_MESSAGE, id, ex);
    return ResponseEntity
        .status(HttpStatus.UNAUTHORIZED)
        .body(ErrorMapper.getErrorResponse(
            id, ErrorCode.BAD_CREDENTIALS, WRONG_LOGIN_OR_PASSWORD_MESSAGE, request));
  }

  @ExceptionHandler({DisabledException.class, CustomDisabledException.class})
  public ResponseEntity<ErrorResponse> handleDisabled(DisabledException ex, WebRequest request) {
    UUID id = UUID.randomUUID();
    String login;

    if (ex instanceof CustomDisabledException) {
      login = ((CustomDisabledException) ex).getName();
      logError(AUTHENTICATION_ERROR_MESSAGE, id, login, ex);
    } else {
      login = null;
      logError(AUTHENTICATION_ERROR_MESSAGE, id, ex);
    }

    return ResponseEntity
        .status(HttpStatus.UNAUTHORIZED)
        .body(ErrorMapper.getErrorResponse(
            id,
            ErrorCode.ACCOUNT_DELETED,
            "Аккаунт удалён",
            login,
            request));
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
            id, ErrorCode.UNCLASSIFIED, ex.getMessage(), request));
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
            id, ErrorCode.UNCLASSIFIED, "Осталось попыток: " + ex.getRemainingAttempts(), request));
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
            id, ErrorCode.INTERNAL_SERVER_ERROR, "Неожиданная внутренняя ошибка", request));
  }

  private void logError(String message, UUID id, Exception ex) {
    final String logMessage = "%s (errorId=%s): ".formatted(message, id);
    log.error(logMessage, ex);
  }

  private void logError(String message, UUID id, String objectDetails, Exception ex) {
    final String logMessage = "%s (errorId=%s, objectDetails=%s): "
        .formatted(message, id, objectDetails);
    log.error(logMessage, ex);
  }
}
