package ru.hh.kakdela_v2.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;

@Service
public class JwtUtil {

  private final SecretKey key;

  public JwtUtil(@Value("${app.jwt-secret}") String SECRET) {
    key = Keys.hmacShaKeyFor(SECRET.getBytes());
  }

  public String extractLogin(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    return claimsResolver.apply(claims);
  }

  private Boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  public String generateAccessToken(UserDetails userDetails) {
    return Jwts.builder()
        .subject(userDetails.getUsername())
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 15))
        .signWith(key)
        .compact();
  }

  public Boolean validateToken(String token, UserDetails userDetails) {
    return (extractLogin(token).equals(userDetails.getUsername()) && !isTokenExpired(token));
  }
}
