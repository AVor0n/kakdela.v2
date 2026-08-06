package ru.hh.kakdela.v2.service;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela.v2.dto.auth.VerifyCodeResponseDto;
import ru.hh.kakdela.v2.exception.ResetCodeException;

@Service
@RequiredArgsConstructor
public class VerificationCodeService {

  private final StringRedisTemplate redisTemplate;

  private static final String codeResetPrefix = "reset:code:";
  private static final String blockResetPrefix = "reset:block:";

  @Value("${app.pwdrest.max_attempts}")
  private int maxAttempts;
  @Value("${app.redis.ttl.code}")
  private long codeExpirationMinutes;
  @Value("${app.redis.ttl.block}")
  private long blockExpirationMinutes;

  public void saveVerificationCode(String email, String code) {
    String codeHashKey = codeResetPrefix + email;
    String blockHashKey = blockResetPrefix + email;
    HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();

    long prevBlockExp = 0;
    if (redisTemplate.hasKey(blockHashKey)) {
      String blockedUntilStr = hashOps.get(blockHashKey, "blocked_until");

      long blockedUntil = Long.parseLong(blockedUntilStr != null ? blockedUntilStr : "0");
      long currentTime = System.currentTimeMillis();
      if (blockedUntil > currentTime) {
        throw new ResetCodeException(
            "Запрещено получать новый код ещё: " + (blockedUntil - currentTime)/6000 + " минут",
            0,
            HttpStatus.TOO_MANY_REQUESTS
        );
      }

      String prevBlockExpStr = hashOps.get(blockHashKey, "block_exp");
      prevBlockExp = Long.parseLong(prevBlockExpStr != null ? prevBlockExpStr : "0");

      deleteVerificationCode(email);
      deleteBlockInfo(email);
    }

    hashOps.put(codeHashKey, "code", code);
    hashOps.put(codeHashKey, "attempts", "0");

    long blockExp = getNextBlockExpiration(prevBlockExp);
    long blockUntil = System.currentTimeMillis()
        + TimeUnit.MINUTES.toMillis(blockExp);
    hashOps.put(blockHashKey, "blocked_until", String.valueOf(blockUntil));
    hashOps.put(blockHashKey, "block_exp", String.valueOf(blockExp));

    redisTemplate.expire(codeHashKey, codeExpirationMinutes, TimeUnit.MINUTES);
    redisTemplate.expire(blockHashKey, blockExpirationMinutes, TimeUnit.MINUTES);
  }

  public VerifyCodeResponseDto verifyCode(String email, String inputCode) {
    String hashKey = codeResetPrefix + email;
    HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();

    if (!redisTemplate.hasKey(hashKey)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Код не найден или истек");
    }

    String attemptsStr = hashOps.get(hashKey, "attempts");
    int attemptsCount = Integer.parseInt(attemptsStr != null ? attemptsStr : "0");
    hashOps.put(hashKey, "attempts", String.valueOf(++attemptsCount));

    if (attemptsCount > maxAttempts) {
      deleteVerificationCode(email);
      throw new ResetCodeException(
          "Превышено число попыток ввода",
          0,
          HttpStatus.TOO_MANY_REQUESTS
      );
    }

    String storedCode = hashOps.get(hashKey, "code");
    if (storedCode == null || !storedCode.equals(inputCode)) {
      throw new ResetCodeException(
          "Неверный код",
          maxAttempts - attemptsCount,
          HttpStatus.BAD_REQUEST
      );
    }

    return new VerifyCodeResponseDto(
        true,
        "Код подтвержден"
    );
  }

  public boolean verifyAndDelete(String email, String inputCode) {
    String codeHashKey = codeResetPrefix + email;
    HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();

    if (!redisTemplate.hasKey(codeHashKey)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Код не найден или истек");
    }

    String storedCode = hashOps.get(codeHashKey, "code");
    deleteVerificationCode(email);

    return storedCode.equals(inputCode);
  }

  private void deleteVerificationCode(String email) {
    String redisKey = codeResetPrefix + email;
    redisTemplate.delete(redisKey);
  }

  private void deleteBlockInfo(String email) {
    String redisKey = blockResetPrefix + email;
    redisTemplate.delete(redisKey);
  }

  private long getNextBlockExpiration(long prev) {
    if (prev==0) return 1;
    if (prev==1) return 5;
    if (prev==5) return 15;
    if (prev==15) return 60;
    return 1440;
  }
}
