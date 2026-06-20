package ru.hh.kakdela_v2.filter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.hh.kakdela_v2.dto.error.ErrorResponse;
import ru.hh.kakdela_v2.util.JwtUtil;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final UserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain chain)
      throws ServletException, IOException {

    Cookie[] cookies = request.getCookies();
    String token = null;

    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if ("accessToken".equals(cookie.getName())) {
          token = cookie.getValue();
        }
      }
    }

    try {
      if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        String login = jwtUtil.extractSubject(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(login);
        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
      }

      chain.doFilter(request, response);
    } catch (ExpiredJwtException e) {
      log.warn("Expired JWT token for user: {}", e.getClaims().getSubject());
      sendErrorResponse(
          response,
          request,
          "JWT token has expired",
          HttpStatus.valueOf(HttpServletResponse.SC_UNAUTHORIZED));
    } catch (MalformedJwtException ex) {
      log.warn("Malformed JWT token");
      sendErrorResponse(response, request, "Invalid JWT token format", HttpStatus.BAD_REQUEST);
    } catch (SignatureException ex) {
      log.warn("Invalid JWT signature");
      sendErrorResponse(response, request, "Invalid token signature", HttpStatus.UNAUTHORIZED);
    } catch (JwtException ex) {
      log.error("JWT processing error", ex);
      sendErrorResponse(response, request, "JWT processing failed", HttpStatus.UNAUTHORIZED);
    }
  }

  private void sendErrorResponse(HttpServletResponse response,
                                 HttpServletRequest request,
                                 String message,
                                 HttpStatus status) throws IOException {

    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    ErrorResponse errResponse = new ErrorResponse(
        LocalDateTime.now(),
        status.value(),
        status.getReasonPhrase(),
        message,
        request.getRequestURI(),
        null
    );

    new ObjectMapper().writeValue(response.getOutputStream(), errResponse);
  }
}