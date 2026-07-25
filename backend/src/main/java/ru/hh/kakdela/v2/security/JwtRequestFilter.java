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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.hh.kakdela.v2.dao.AccountDao;
import ru.hh.kakdela.v2.model.Account;
import ru.hh.kakdela.v2.util.CookieUtil;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final CustomUserDetailsService customUserDetailsService;
  private final AuthenticationEntryPoint authenticationEntryPoint;
  private final AccountDao accountDao;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain chain) throws ServletException, IOException {

    final String token = CookieUtil.getAccessToken(request);
    try {
      if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {

        Claims claims = jwtService.extractAllClaims(token);

        String login = claims.getSubject();
        UUID accountId = UUID.fromString(claims.get("accountId", String.class));
        Integer tokenVersion = claims.get("tokenVersion", Integer.class);

        Account account = accountDao.findByLogin(login).orElseThrow(() ->
            new UsernameNotFoundException("Аккаунт не найден"));

        if (account.getIsDeleted() != null && account.getIsDeleted()) {
          throw new BadCredentialsException("Аккаунт удалён");
        }

        if (!account.getId().equals(accountId)) {
          throw new BadCredentialsException("ID пользователя не совпадает");
        }

        int currentVersion = account.getTokenVersion() != null ? account.getTokenVersion() : 1;
        if (tokenVersion != currentVersion) {
          throw new CredentialsExpiredException("Версия токена устарела");
        }

        UserDetails userDetails = customUserDetailsService.toUserDetails(account);

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