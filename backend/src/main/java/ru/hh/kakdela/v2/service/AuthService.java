package ru.hh.kakdela.v2.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela.v2.dao.AccountDao;
import ru.hh.kakdela.v2.dto.auth.AuthTokensDto;
import ru.hh.kakdela.v2.dto.auth.LoginDto;
import ru.hh.kakdela.v2.model.Account;
import ru.hh.kakdela.v2.security.JwtService;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {

  private final AuthenticationManager authenticationManager;
  private final PasswordEncoder passwordEncoder;
  private final AccountDao accountDao;
  private final JwtService jwtService;
  private final RefreshTokenService refreshTokenService;

  private Account authenticate(LoginDto loginDto) {
    Account account = accountDao.findByLogin(loginDto.getLogin()).orElseThrow(() ->
        new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный логин или пароль"));

    if (account.getIsDeleted() != null && account.getIsDeleted()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Аккаунт удалён");
    }

    try {
      authenticationManager
          .authenticate(
              new UsernamePasswordAuthenticationToken(
                  loginDto.getLogin(),
                  loginDto.getPassword()));
    } catch (AuthenticationException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный логин или пароль");
    }

    log.info("Успешная аутентификация: login={}", loginDto.getLogin());
    return account;
  }

  @Transactional
  public AuthTokensDto login(
      LoginDto loginDto,
      String deviceId,
      String userAgent,
      String ipAddress) {

    Account account = authenticate(loginDto);

    refreshTokenService.revokeAllByAccountIdAndDeviceId(account.getId(), deviceId);

    String refreshToken = refreshTokenService
        .createRefreshToken(account.getId(), deviceId, userAgent, ipAddress);
    String accessToken = jwtService.generateAccessToken(account);

    log.info("Успешный вход: accountId={}, deviceId={}", account.getId(), deviceId);

    return new AuthTokensDto(accessToken, refreshToken);
  }

  @Transactional
  public AuthTokensDto refreshTokens(
      String refreshToken,
      String deviceId,
      String userAgent,
      String ipAddress
  ) {
    if (refreshToken == null || refreshToken.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh токен не найден");
    }

    if (deviceId == null || deviceId.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "DeviceId не найден");
    }

    Account account = refreshTokenService.getAccountByToken(refreshToken);

    String newRefreshToken = refreshTokenService
        .rotateRefreshToken(refreshToken, deviceId, userAgent, ipAddress);

    String newAccessToken = jwtService.generateAccessToken(account);

    log.info("Обновлён access токен для accountId={}, deviceId={}", account.getId(), deviceId);

    return new AuthTokensDto(newAccessToken, newRefreshToken);
  }

  @Transactional
  public void logout(String refreshToken) {
    if (refreshToken != null) {
      refreshTokenService.revokeByToken(refreshToken);
      log.info("Отозван refresh токен");
    }
  }

  @Transactional
  public void logoutEverywhere(UUID accountId) {
    refreshTokenService.revokeAllByAccountId(accountId);
    incrementTokenVersion(accountId);
    log.info("Выход везде для accountId={}", accountId);
  }

  public void checkPassword(UserDetails userDetails, String providedPassword) {
    if (!passwordEncoder.matches(providedPassword, userDetails.getPassword())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Предоставлен неверный пароль");
    }
  }

  @Transactional
  public void incrementTokenVersion(UUID accountId) {
    Account account = accountDao.findById(accountId).orElseThrow(() ->
        new ResponseStatusException(HttpStatus.NOT_FOUND, "Аккаунт не найден: " + accountId));

    int newVersion = account.getTokenVersion() + 1;
    account.setTokenVersion(newVersion);

    accountDao.update(account);

    log.info(
        "Повышена версия токена для accountId={}: {} → {}",
        accountId,
        account.getTokenVersion() - 1,
        newVersion);
  }
}
