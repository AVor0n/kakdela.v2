package ru.hh.kakdela.v2.mapper;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import ru.hh.kakdela.v2.dto.error.ErrorResponse;
import ru.hh.kakdela.v2.exception.ErrorCode;
import ru.hh.kakdela.v2.exception.Kd2AuthenticationException;
import ru.hh.kakdela.v2.exception.Kd2Exception;
import ru.hh.kakdela.v2.exception.Kd2ObjectRelatedException;

public class ErrorMapper {

  public static ErrorResponse getErrorResponse(
      UUID id,
      ErrorCode errorCode,
      String message,
      WebRequest request
  ) {
    return new ErrorResponse(
        LocalDateTime.now(),
        errorCode,
        id,
        message,
        null,
        null,
        null,
        getPath(request));
  }

  public static ErrorResponse getErrorResponse(
      UUID id,
      ErrorCode errorCode,
      String message,
      String objectDetails,
      WebRequest request
  ) {
    return new ErrorResponse(
        LocalDateTime.now(),
        errorCode,
        id,
        message,
        null,
        null,
        objectDetails,
        getPath(request));
  }

  public static ErrorResponse getErrorResponse(
      UUID id,
      ErrorCode errorCode,
      String message,
      UUID object1Id,
      UUID object2Id,
      String objectDetails,
      WebRequest request
  ) {
    return new ErrorResponse(
        LocalDateTime.now(),
        errorCode,
        id,
        message,
        object1Id,
        object2Id,
        objectDetails,
        getPath(request));
  }

  public static ErrorResponse getErrorResponse(
      UUID id,
      Kd2Exception ex,
      WebRequest request) {
    return new ErrorResponse(
        LocalDateTime.now(),
        ex.getErrorCode(),
        id,
        ex.getMessage(),
        null,
        null,
        null,
        getPath(request));
  }

  public static ErrorResponse getErrorResponse(
      UUID id,
      Kd2AuthenticationException ex,
      WebRequest request) {
    return new ErrorResponse(
        LocalDateTime.now(),
        ex.getErrorCode(),
        id,
        ex.getMessage(),
        null,
        null,
        ex.getObjectDetails(),
        getPath(request));
  }

  public static ErrorResponse getErrorResponse(
      UUID id,
      Kd2ObjectRelatedException ex,
      WebRequest request) {
    return new ErrorResponse(
        LocalDateTime.now(),
        ex.getErrorCode(),
        id,
        ex.getMessage(),
        ex.getObject1Id(),
        ex.getObject2Id(),
        ex.getObjectDetails(),
        getPath(request));
  }

  private static String getPath(WebRequest request) {
    if (request instanceof ServletWebRequest) {
      HttpServletRequest servletRequest = ((ServletWebRequest) request).getRequest();
      return servletRequest.getRequestURI();
    }
    return null;
  }
}
