package ru.hh.kakdela.v2.filter;

import java.io.IOException;
import java.util.UUID;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.hh.kakdela.v2.security.CustomUserDetails;

@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
    String requestId = UUID.randomUUID().toString().substring(0, 8);
    long startTime = System.currentTimeMillis();

    MDC.put("requestId", requestId);

    try {
      log.info(">>> {} {} from {}",
          request.getMethod(),
          request.getRequestURI(),
          request.getRemoteAddr());

      filterChain.doFilter(request, response);

      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
        MDC.put("userId", userDetails.getId().toString());
      }

      long duration = System.currentTimeMillis() - startTime;

      log.info("<<< {} {} → {} ({}ms)",
          request.getMethod(),
          request.getRequestURI(),
          response.getStatus(),
          duration);

    } finally {
      MDC.clear();
    }
  }
}
