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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import ru.hh.kakdela.v2.constants.ErrorMessages;
import ru.hh.kakdela.v2.dto.error.ErrorResponse;
import ru.hh.kakdela.v2.exception.ErrorCode;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException
  ) throws IOException, ServletException {

    response.setStatus(HttpStatus.FORBIDDEN.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    UUID id = UUID.randomUUID();

    log.error(ErrorMessages.ACCESS_DENIED_MESSAGE + " (errorId={}):", id, accessDeniedException);

    ErrorResponse errorResponse = new ErrorResponse(
        getTimestamp(),
        ErrorCode.ACCESS_DENIED,
        id,
        ErrorMessages.ACCESS_DENIED_MESSAGE,
        null,
        null,
        null,
        request.getRequestURI());

    new ObjectMapper().writeValue(
        response.getOutputStream(),
        errorResponse);
  }
}
