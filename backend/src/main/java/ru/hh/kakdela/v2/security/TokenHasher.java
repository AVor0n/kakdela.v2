package ru.hh.kakdela.v2.security;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public final class TokenHasher {

  private static final String ALGORITHM = "SHA-256";
  private static final String TOKEN_PREFIX = "ref_";

  private static final SecureRandom SECURE_RANDOM = new SecureRandom();
  private static final int TOKEN_BYTES_LENGTH = 32;


  public String generateRawToken() {
    byte[] bytes = new byte[TOKEN_BYTES_LENGTH];
    SECURE_RANDOM.nextBytes(bytes);

    String randomPart = Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(bytes);

    return TOKEN_PREFIX + randomPart;
  }

  public String hash(String rawToken) {
    if (rawToken == null) {
      throw new IllegalArgumentException("Токен не может быть null");
    }

    try {
      MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
      byte[] encodedHash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
      return bytesToHex(encodedHash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(
          "Критическая ошибка: алгоритм " + ALGORITHM + " не поддерживается JVM. " +
          "Проверьте установку Java.", e
      );
    }
  }

  public boolean matches(String rawToken, String hash) {
    if (rawToken == null || hash == null) {
      return false;
    }
    return hash(rawToken).equals(hash);
  }

  private String bytesToHex(byte[] hash) {
    StringBuilder hexString = new StringBuilder(2 * hash.length);
    for (byte b : hash) {
      String hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }
    return hexString.toString();
  }
}
