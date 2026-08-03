package ru.hh.kakdela.v2.service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela.v2.dao.AccountDao;
import ru.hh.kakdela.v2.dao.RefreshTokenDao;
import ru.hh.kakdela.v2.dto.auth.RefreshTokenDto;
import ru.hh.kakdela.v2.model.Account;
import ru.hh.kakdela.v2.model.RefreshToken;
import ru.hh.kakdela.v2.util.TokenUtil;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  private final RefreshTokenDao refreshTokenDao;
  private final AccountDao accountDao;
  private final Clock clock;

  @Value("${app.tokens.refresh.max-age}")
  private long refreshTokenMaxAge;

  @Transactional
  public String createRefreshToken(
      UUID accountId,
      String deviceId,
      String userAgent,
      String ipAddress) {
    Account account = accountDao.findById(accountId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Аккаунт не найден"));

    RefreshTokenDto dto = getNewRefreshToken(
        account,
        deviceId,
        userAgent,
        ipAddress);

    refreshTokenDao.save(dto.refreshToken());

    return dto.rawRefreshToken();
  }

  @Transactional(readOnly = true)
  public Account getAccountByToken(String rawToken) {
    String tokenHash = TokenUtil.hash(rawToken);
    RefreshToken refreshToken = refreshTokenDao.findByTokenHash(tokenHash)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.UNAUTHORIZED, "Невалидный refresh токен"));

    Instant now = Instant.now(clock);
    if (now.isAfter(refreshToken.getExpiresAt())) {
      throw new ResponseStatusException(
          HttpStatus.UNAUTHORIZED, "Refresh токен истёк");
    }

    return refreshToken.getAccount();
  }

  @Transactional
  public String rotateRefreshToken(
      String oldRawToken,
      String deviceId,
      String userAgent,
      String ipAddress) {
    RefreshToken oldRefreshToken = validateToken(oldRawToken, deviceId);
    Account account = oldRefreshToken.getAccount();

    refreshTokenDao.delete(oldRefreshToken);
    log.debug("Удалён старый refresh токен при ротации для accountId={}", account.getId());

    RefreshTokenDto dto = getNewRefreshToken(account, deviceId, userAgent, ipAddress);
    refreshTokenDao.save(dto.refreshToken());
    log.info("Произведена ротация refresh токена для accountId={}, deviceId={}",
        account.getId(), deviceId);

    return dto.rawRefreshToken();
  }

  @Transactional
  public void revokeByToken(String rawToken) {
    String tokenHash = TokenUtil.hash(rawToken);
    refreshTokenDao.deleteByTokenHash(tokenHash);
    log.info("Отозван refresh токен");
  }

  @Transactional
  public void revokeAllByAccountId(UUID accountId) {
    refreshTokenDao.deleteAllByAccountId(accountId);
    log.info("Отозваны все refresh токены для accountId={}", accountId);
  }

  @Transactional
  public void revokeAllByAccountIdAndDeviceId(UUID accountId, String deviceId) {
    refreshTokenDao.deleteAllByAccountIdAndDeviceId(accountId, deviceId);
    log.info("Отозваны refresh токены для accountId={}, deviceId={}",
        accountId, deviceId);
  }

  @Transactional(readOnly = true)
  public List<RefreshToken> getActiveTokensByAccountId(UUID accountId) {
    Instant now = Instant.now(clock);
    return refreshTokenDao.findActiveByAccountId(accountId, now);
  }

  @Transactional
  public void cleanExpiredTokens() {
    Instant now = Instant.now(clock);
    refreshTokenDao.deleteAllExpired(now);
    log.info("Очищены истекшие refresh токены");
  }

  // Вспомогательные методы

  private RefreshToken validateToken(String rawToken, String deviceId) {
    String tokenHash = TokenUtil.hash(rawToken);
    RefreshToken refreshToken = refreshTokenDao.findByTokenHash(tokenHash)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.UNAUTHORIZED, "Невалидный refresh токен"));

    Instant now = Instant.now(clock);
    if (now.isAfter(refreshToken.getExpiresAt())) {
      refreshTokenDao.delete(refreshToken);
      throw new ResponseStatusException(
          HttpStatus.UNAUTHORIZED, "Refresh токен истёк");
    }

    if (!refreshToken.getDeviceId().equals(deviceId)) {
      log.warn("Несовпадение deviceId для токена: ожидался {}, получен {}",
          refreshToken.getDeviceId(), deviceId);
      throw new ResponseStatusException(
          HttpStatus.UNAUTHORIZED, "Устройство не совпадает");
    }

    refreshToken.setLastUsedAt(now);
    refreshTokenDao.update(refreshToken);

    return refreshToken;
  }

  private RefreshTokenDto getNewRefreshToken(
      Account account,
      String deviceId,
      String userAgent,
      String ipAddress
  ) {
    String rawToken = TokenUtil.generateRawToken();
    String tokenHash = TokenUtil.hash(rawToken);

    Instant now = Instant.now(clock);
    RefreshToken refreshToken = RefreshToken.builder()
        .tokenHash(tokenHash)
        .account(account)
        .deviceId(deviceId)
        .userAgent(userAgent)
        .ipAddress(ipAddress)
        .createdAt(now)
        .expiresAt(now.plus(refreshTokenMaxAge, ChronoUnit.SECONDS))
        .build();

    return new RefreshTokenDto(refreshToken, rawToken);
  }
}
