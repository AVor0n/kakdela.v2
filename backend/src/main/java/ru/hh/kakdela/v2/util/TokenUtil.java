package ru.hh.kakdela.v2.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

public final class TokenUtil{

  private static final String ALGORITHM = "SHA-256";
  private static final String TOKEN_PREFIX = "ref_";
  private static final int TOKEN_BYTES_LENGTH = 32;

  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private TokenUtil() {}

  public static String generateRawToken() {
    byte[] bytes = new byte[TOKEN_BYTES_LENGTH];
    SECURE_RANDOM.nextBytes(bytes);

    String randomPart = Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(bytes);

    return TOKEN_PREFIX + randomPart;
  }

  public static String hash(String rawToken) {
    if (rawToken == null) {
      throw new IllegalArgumentException("Токен не может быть null");
    }

    try {
      MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
      byte[] encodedHash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(encodedHash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(
          "Критическая ошибка: алгоритм " + ALGORITHM
              + " не поддерживается JVM. Проверьте установку Java.", e
      );
    }
  }

  public static boolean matches(String rawToken, String hash) {
    if (rawToken == null || hash == null) {
      return false;
    }
    return hash(rawToken).equals(hash);
  }
}
