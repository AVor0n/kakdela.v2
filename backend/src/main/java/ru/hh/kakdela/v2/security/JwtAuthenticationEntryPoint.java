package ru.hh.kakdela.v2.security;

import static ru.hh.kakdela.v2.mapper.ErrorMapper.getTimestamp;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import ru.hh.kakdela.v2.constants.ErrorMessages;
import ru.hh.kakdela.v2.dto.error.ErrorResponse;
import ru.hh.kakdela.v2.exception.ErrorCode;
import ru.hh.kakdela.v2.exception.Kd2AuthenticationException;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException
  ) throws IOException, ServletException {

    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    UUID id = UUID.randomUUID();

    if (authException instanceof Kd2AuthenticationException
        && ((Kd2AuthenticationException) authException).getObjectDetails() != null) {
      log.error(ErrorMessages.JWT_AUTHENTICATION_ERROR_MESSAGE + " (errorId={}, objectDetails={}):",
          id, ((Kd2AuthenticationException) authException).getObjectDetails(), authException);
    } else if (authException.getCause() != null
        && authException.getCause() instanceof UsernameNotFoundException
        && ((UsernameNotFoundException) authException.getCause()).getName() != null) {
      log.error(ErrorMessages.JWT_AUTHENTICATION_ERROR_MESSAGE + " (errorId={}, objectDetails={}):",
          id, ((UsernameNotFoundException) authException.getCause()).getName(), authException);
    } else {
      log.error(
          ErrorMessages.JWT_AUTHENTICATION_ERROR_MESSAGE + " (errorId={}):", id, authException);
    }

    ErrorResponse errorResponse;

    if (authException instanceof Kd2AuthenticationException) {
      errorResponse = new ErrorResponse(
          getTimestamp(),
          ((Kd2AuthenticationException) authException).getErrorCode(),
          id,
          authException.getMessage(),
          null,
          null,
          ((Kd2AuthenticationException) authException).getObjectDetails(),
          request.getRequestURI());
    } else {
      ErrorCode errorCode;
      String message;

      if (authException instanceof InsufficientAuthenticationException) {
        errorCode = ErrorCode.AUTHENTICATION_REQUIRED;
        message = ErrorMessages.AUTHENTICATION_REQUIRED_MESSAGE;
      } else {
        errorCode = ErrorCode.AUTHENTICATION_ERROR;
        message = authException.getMessage();
      }

      errorResponse = new ErrorResponse(
          getTimestamp(),
          errorCode,
          id,
          message,
          null,
          null,
          null,
          request.getRequestURI());
    }

    new ObjectMapper().writeValue(
        response.getOutputStream(),
        errorResponse);
  }
}
