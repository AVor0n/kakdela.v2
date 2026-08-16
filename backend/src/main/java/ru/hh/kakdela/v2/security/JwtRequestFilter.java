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
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.hh.kakdela.v2.exception.security.AccountDeletedException;
import ru.hh.kakdela.v2.exception.security.ExpiredAccessTokenException;
import ru.hh.kakdela.v2.exception.security.InvalidAccessTokenAccountIdException;
import ru.hh.kakdela.v2.exception.security.InvalidAccessTokenException;
import ru.hh.kakdela.v2.exception.security.InvalidAccessTokenVersionException;
import ru.hh.kakdela.v2.service.AuthCookieService;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final CustomUserDetailsService customUserDetailsService;
  private final AuthenticationEntryPoint authenticationEntryPoint;
  private final AuthCookieService authCookieService;

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
          throw new InvalidAccessTokenAccountIdException();
        }

        if (userDetails.getTokenVersion() != tokenVersionFromToken) {
          throw new InvalidAccessTokenVersionException();
        }

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
      }

      chain.doFilter(request, response);

    } catch (DisabledException ex) {
      String login;

      if (ex instanceof CustomDisabledException) {
        login = ((CustomDisabledException) ex).getName();
      } else {
        login = null;
      }

      SecurityContextHolder.clearContext();
      authenticationEntryPoint.commence(request, response,
          new AccountDeletedException(login));
    } catch (InvalidAccessTokenVersionException | ExpiredJwtException ex) {
      SecurityContextHolder.clearContext();
      authenticationEntryPoint.commence(request, response,
          new ExpiredAccessTokenException(ex));
    } catch (UsernameNotFoundException | InvalidAccessTokenAccountIdException
             | MalformedJwtException | SignatureException ex) {
      SecurityContextHolder.clearContext();
      authenticationEntryPoint.commence(request, response,
          new InvalidAccessTokenException(ex));
    } catch (AuthenticationException ex) {
      SecurityContextHolder.clearContext();
      authenticationEntryPoint.commence(request, response, ex);
    } catch (JwtException ex) {
      SecurityContextHolder.clearContext();
      authenticationEntryPoint.commence(request, response,
          new InternalAuthenticationServiceException(
              "Неожиданная внутренняя ошибка обработки access token"));
    }
  }
}
