package ru.hh.kakdela.v2.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final SecretKey key;

  @Value("${app.tokens.access.max-age}")
  private long accessTokenMaxAge;

  @Value("${app.tokens.response-access.max-age}")
  private long responseAccessTokenMaxAge;

  public JwtService(@Value("${app.jwt.secret}") String secret) {
    key = Keys.hmacShaKeyFor(secret.getBytes());
  }

  public String extractSubject(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public UUID extractResponseId(String token) {
    return UUID.fromString(
        extractClaim(token, claims -> claims.get("responseId", String.class)));
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload();
    return claimsResolver.apply(claims);
  }

  public String generateAccessToken(String login) {
    return Jwts.builder()
        .subject(login)
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + 1000 * accessTokenMaxAge))
        .signWith(key)
        .compact();
  }

  public String generateResponseAccessToken(UUID responseId) {
    return Jwts.builder()
        .claim("responseId", responseId)
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + 1000 * responseAccessTokenMaxAge))
        .signWith(key)
        .compact();
  }
}
