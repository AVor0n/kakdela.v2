package ru.hh.kakdela.v2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class VerificationCodeService {

  private final StringRedisTemplate redisTemplate;

  private static final long CODE_EXPIRATION_MINUTES = 15;

  public void saveVerificationCode(String email, String code) {
    String redisKey = "reset:code:" + email;
    redisTemplate.opsForValue().set(redisKey, code, CODE_EXPIRATION_MINUTES, TimeUnit.MINUTES);
  }

  public String getVerificationCode(String email) {
    String redisKey = "reset:code:" + email;
    return redisTemplate.opsForValue().get(redisKey);
  }

  public boolean verifyCode(String email, String inputCode) {
    String storedCode = getVerificationCode(email);
    return storedCode != null && storedCode.equals(inputCode);
  }

  public void deleteVerificationCode(String email) {
    String redisKey = "reset:code:" + email;
    redisTemplate.delete(redisKey);
  }
}
