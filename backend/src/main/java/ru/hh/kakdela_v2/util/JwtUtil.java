package ru.hh.kakdela_v2.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;

@Service()
public class JwtUtil {

  private final SecretKey key;

  public JwtUtil(@Value("${app.jwt-secret}") String SECRET) {
    key = Keys.hmacShaKeyFor(SECRET.getBytes());
  }

  public String extractTokenFromHeader(String header) {
    String token = null;

    if (header != null && header.startsWith("Bearer ")) {
      token = header.substring(7);
    }

    return token;
  }

  public String extractSubject(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    return claimsResolver.apply(claims);
  }

  public String generateAccessToken(UserDetails userDetails) {
    return Jwts.builder()
        .subject(userDetails.getUsername())
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 15))
        .signWith(key)
        .compact();
  }

  public String generateResponseEditToken(UUID responseId) {
    return Jwts.builder()
        .subject(responseId.toString())
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7))
        .signWith(key)
        .compact();
  }
}
