package ru.hh.kakdela.v2.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.hh.kakdela.v2.model.Account;

@Slf4j
@Service
public class JwtService {

  private final SecretKey secretKey;
  private final long accessTokenMaxAgeSeconds;
  private final long responseTokenMaxAgeSeconds;
  private final Clock clock;

  public JwtService(
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.tokens.access.max-age}") long accessTokenMaxAge,
      @Value("${app.tokens.response-access.max-age}") long responseTokenMaxAge,
      Clock clock) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    this.accessTokenMaxAgeSeconds = accessTokenMaxAge;
    this.responseTokenMaxAgeSeconds = responseTokenMaxAge;
    this.clock = clock;
  }

  public String generateAccessToken(Account account) {
    Instant now = Instant.now(clock);
    Instant expiresAt = now.plus(accessTokenMaxAgeSeconds, ChronoUnit.SECONDS);

    return Jwts.builder()
        .subject(account.getLogin())
        .claim("accountId", account.getId())
        .claim("tokenVersion", account.getTokenVersion() != null ? account.getTokenVersion() : 1)
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiresAt))
        .signWith(secretKey)
        .compact();
  }

  public String generateResponseAccessToken(UUID responseId) {
    Instant now = Instant.now(clock);
    Instant expireAt = now.plus(responseTokenMaxAgeSeconds, ChronoUnit.SECONDS);

    return Jwts.builder()
        .claim("responseId", responseId)
        .issuedAt(Date.from(now))
        .expiration(Date.from(expireAt))
        .signWith(secretKey)
        .compact();
  }

  public Claims extractAllClaims(String token) {
    return Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  public String extractSubject(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public UUID extractAccountId(String token) {
    return extractClaim(token, claims -> {
      String accountIdStr = claims.get("accountId", String.class);

      return accountIdStr != null ? UUID.fromString(accountIdStr) : null;
    });
  }

  public int extractTokenVersion(String token) {
    return extractClaim(token, claims -> {
      Integer version = claims.get("tokenVersion", Integer.class);

      return version != null ? version : 1;
    });
  }

  public UUID extractResponseId(String token) {
    return extractClaim(token, claims -> {
      String responseIdStr = claims.get("responseId", String.class);

      return responseIdStr != null ? UUID.fromString(responseIdStr) : null;
    });
  }

  public boolean validateToken(String token) {
    try {
      extractAllClaims(token);
      return true;
    } catch (ExpiredJwtException e) {
      log.debug("Токен истёк: {}", e.getMessage());
      return false;
    } catch (MalformedJwtException e) {
      log.debug("Неверный формат токена: {}", e.getMessage());
      return false;
    } catch (SignatureException e) {
      log.debug("Неверная подпись токена: {}", e.getMessage());
      return false;
    } catch (JwtException e) {
      log.debug("Ошибка валидации токена: {}", e.getMessage());
      return false;
    }
  }
}
