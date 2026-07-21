package ru.hh.kakdela.v2.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.hh.kakdela.v2.constants.CookieNames;
import ru.hh.kakdela.v2.util.CookieUtil;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;
  private final AuthenticationEntryPoint authenticationEntryPoint;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain chain) throws ServletException, IOException {

    final String token = CookieUtil.getCookieValueByName(request, CookieNames.accessToken);

    try {
      if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        String login = jwtService.extractSubject(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(login);
        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
      }

      chain.doFilter(request, response);
    } catch (UsernameNotFoundException ex) {
      log.warn("Аккаунт не найден: login={}", ex.getName());
      SecurityContextHolder.clearContext();
      authenticationEntryPoint.commence(request, response,
          new UsernameNotFoundException("Account not found: login=%s".formatted(ex.getName())));
    } catch (ExpiredJwtException ex) {
      log.warn("Срок действия токена JWT для пользователя истек: {}", ex.getClaims().getSubject());
      SecurityContextHolder.clearContext();
      authenticationEntryPoint.commence(request, response,
          new CredentialsExpiredException("Expired JWT"));
    } catch (MalformedJwtException | SignatureException ex) {
      log.warn("Неверный токен JWT");
      SecurityContextHolder.clearContext();
      authenticationEntryPoint.commence(request, response,
          new BadCredentialsException("Invalid JWT"));
    } catch (JwtException ex) {
      SecurityContextHolder.clearContext();
      authenticationEntryPoint.commence(request, response,
          new InternalAuthenticationServiceException("Unexpected JWT processing error"));
    }
  }
}