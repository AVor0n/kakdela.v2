package ru.hh.kakdela.v2.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.hh.kakdela.v2.service.AuthCookieService;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final CustomUserDetailsService customUserDetailsService;
  private final AuthenticationEntryPoint authenticationEntryPoint;
  private final AuthCookieService authCookieService;
  @Value("${app.oauth2.callback-base-uri:/api/auth/oauth2/callback/*}")
  private String oauth2CallbackBaseUri;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain chain) throws ServletException, IOException {

    final String token = authCookieService.getAccessToken(request);

    try {
      if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {

        Claims claims = jwtService.extractAllClaims(token);

        String login = claims.getSubject();
        UUID accountId = UUID.fromString(claims.get("accountId", String.class));
        Integer tokenVersionFromToken = claims.get("tokenVersion", Integer.class);

        CustomUserDetails userDetails = customUserDetailsService.loadUserByUsername(login);

        if (!userDetails.getId().equals(accountId)) {
          throw new BadCredentialsException("ID пользователя не совпадает");
        }

        if (userDetails.getTokenVersion() != tokenVersionFromToken) {
          throw new CredentialsExpiredException("Версия токена устарела");
        }

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
      }

      chain.doFilter(request, response);

    } catch (UsernameNotFoundException ex) {
      log.warn("Аккаунт не найден: login={}", ex.getName());
      SecurityContextHolder.clearContext();
      authenticationEntryPoint.commence(request, response,
          new UsernameNotFoundException("Account not found: login=%s".formatted(ex.getName())));
    } catch (DisabledException ex) {
      log.warn("Аккаунт удалён: {}", ex.getMessage());
      SecurityContextHolder.clearContext();
      authenticationEntryPoint.commence(request, response,
          new DisabledException("Account disabled", ex));
    } catch (BadCredentialsException ex) {
      log.warn("Неверные учётные данные: {}", ex.getMessage());
      SecurityContextHolder.clearContext();
      authenticationEntryPoint.commence(request, response, ex);
    } catch (CredentialsExpiredException ex) {
      log.warn("Срок действия токена истёк: {}", ex.getMessage());
      SecurityContextHolder.clearContext();
      authenticationEntryPoint.commence(request, response,
          new CredentialsExpiredException("Token expired", ex));
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

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return new AntPathMatcher().match(oauth2CallbackBaseUri, request.getRequestURI());
  }
}