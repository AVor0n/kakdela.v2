package ru.hh.kakdela.v2.service;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import ru.hh.kakdela.v2.dto.auth.VerifyCodeResponseDto;

@Service
@RequiredArgsConstructor
public class VerificationCodeService {

  private final StringRedisTemplate redisTemplate;

  private static final String resetPrefix = "reset:";
  private static final int maxAttempts = 3;
  private static final long codeExpirationMinutes = 15;
  private static final long blockExpirationMinutes = 60;

  public void saveVerificationCode(String email, String code) {
    String hashKey = resetPrefix + email;

    HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();

    hashOps.put(hashKey, "code", code);
    hashOps.put(hashKey, "attempts", "0");
    hashOps.put(hashKey, "blocked_until", "0");

    redisTemplate.expire(hashKey, codeExpirationMinutes, TimeUnit.MINUTES);
  }

  public VerifyCodeResponseDto verifyCode(String email, String inputCode) {
    String hashKey = resetPrefix + email;
    HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();

    if (!redisTemplate.hasKey(hashKey)) {
      return new VerifyCodeResponseDto(
          false,
          false,
          "Код не найден",
          0
      );
    }

    String storedCode = hashOps.get(hashKey, "code");
    String attemptsStr = hashOps.get(hashKey, "attempts");
    String blockedUntilStr = hashOps.get(hashKey, "blocked_until");

    long blockedUntil = Long.parseLong(blockedUntilStr != null ? blockedUntilStr : "0");
    long currentTime = System.currentTimeMillis();
    if (blockedUntil > currentTime) {
      long remainingMinutes = (blockedUntil - currentTime) / 60000;
      return new VerifyCodeResponseDto(
          false,
          true,
          "Слишком пного попыток, попробуйте через " + remainingMinutes + " минут",
          0
      );
    }

    int attemptsCount = Integer.parseInt(attemptsStr != null ? attemptsStr : "0");
    if (storedCode != null && storedCode.equals(inputCode)) {
      return new VerifyCodeResponseDto(
          true,
          false,
          "Код подтвержден",
          0
      );
    }

    hashOps.put(hashKey, "attempts", String.valueOf(++attemptsCount));

    if (attemptsCount >= maxAttempts) {
      long blockUntil = System.currentTimeMillis()
          + TimeUnit.MINUTES.toMillis(blockExpirationMinutes);
      hashOps.put(hashKey, "blocked_until", String.valueOf(blockUntil));

      redisTemplate.expire(hashKey, blockExpirationMinutes, TimeUnit.MINUTES);

      return new VerifyCodeResponseDto(
          false,
          true,
          "Превышено количество попыток. Попробуйте через час",
          0
        );
    }

    return new VerifyCodeResponseDto(
        false,
        true,
        "Неверный код",
        maxAttempts - attemptsCount
    );
  }

  public boolean verifyAndDelete(String email, String inputCode) {
    String hashKey = resetPrefix + email;
    HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();

    if (!redisTemplate.hasKey(hashKey)) {
      return false;
    }

    String storedCode = hashOps.get(hashKey, "code");
    String blockedUntilStr = hashOps.get(hashKey, "blocked_until");
    long blockedUntil = Long.parseLong(blockedUntilStr != null ? blockedUntilStr : "0");
    deleteVerificationCode(email);

    return storedCode.equals(inputCode) && !(blockedUntil > System.currentTimeMillis());
  }

  public void deleteVerificationCode(String email) {
    String redisKey = resetPrefix + email;
    redisTemplate.delete(redisKey);
  }
}
