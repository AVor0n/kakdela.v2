package ru.hh.kakdela.v2.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import ru.hh.kakdela.v2.dto.error.ErrorResponse;
import ru.hh.kakdela.v2.exception.ErrorCode;
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

    log.error("Ошибка аутентификации (errorId={}):", id, authException);

    ErrorResponse errorResponse = new ErrorResponse(
        LocalDateTime.now(),
        ErrorCode.BAD_CREDENTIALS,
        id,
        authException.getMessage(),
        null,
        null,
        request.getRequestURI());

    new ObjectMapper().writeValue(
        response.getOutputStream(),
        errorResponse);
  }
}
