package ru.hh.kakdela.v2.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.hh.kakdela.v2.service.RefreshTokenService;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {

  private final RefreshTokenService refreshTokenService;

  // "0 0 3 * * ?" - каждый день в 3:00
  @Scheduled(cron = "0 0 3 * * ?", zone = "Europe/Moscow")
  public void cleanExpiredTokens() {
    try {
      refreshTokenService.cleanExpiredTokens();
      log.info("Очистка истекших refresh токенов завершена");
    } catch (Exception e) {
      log.error(" Ошибка при очистке истекших refresh токенов", e);
    }
  }
}
