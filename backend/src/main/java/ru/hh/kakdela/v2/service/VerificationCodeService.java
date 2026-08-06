package ru.hh.kakdela.v2.service;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela.v2.dto.auth.VerifyCodeResponseDto;

@Service
@RequiredArgsConstructor
public class VerificationCodeService {

  private final StringRedisTemplate redisTemplate;

  private static final String codeResetPrefix = "reset:code:";
  private static final String blockResetPrefix = "reset:block:";
  private static final int maxAttempts = 3;
  private static final long codeExpirationMinutes = 15;
  private static final long blockExpirationMinutes = 1440;

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
        // TODO: кастомное исключение
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
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
    }

    String storedCode = hashOps.get(hashKey, "code");
    if (storedCode != null && storedCode.equals(inputCode)) {
      return new VerifyCodeResponseDto(
          true,
          false,
          "Код подтвержден",
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
    String codeHashKey = codeResetPrefix + email;
    HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();

    if (!redisTemplate.hasKey(codeHashKey)) {
      return false;
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
