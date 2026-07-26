package ru.hh.kakdela.v2.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import ru.hh.kakdela.v2.util.CookieUtil;
import ru.hh.kakdela.v2.util.DeviceUtil;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {

  private final AuthenticationManager authenticationManager;
  private final PasswordEncoder passwordEncoder;
  private final AccountDao accountDao;
  private final JwtService jwtService;
  private final RefreshTokenService refreshTokenService;
  private final DeviceUtil deviceUtil;

  public Account authenticate(LoginDto loginDto) {
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
      HttpServletRequest request,
      HttpServletResponse response) {

    Account account = authenticate(loginDto);

    String deviceId = deviceUtil.getOrCreateDeviceId(request, response);
    String userAgent = request.getHeader("User-Agent");
    String ipAddress = request.getRemoteAddr();

    refreshTokenService.revokeAllByAccountIdAndDeviceId(account.getId(), deviceId);

    String refreshToken = refreshTokenService
        .createRefreshToken(account.getId(), deviceId, userAgent, ipAddress);

    String accessToken = jwtService.generateAccessToken(account);

    log.info("Успешный вход: accountId={}, deviceId={}", account.getId(), deviceId);

    return new AuthTokensDto(accessToken, refreshToken);
  }

  @Transactional
  public AuthTokensDto refreshTokens(HttpServletRequest request) {
    String refreshToken = CookieUtil.getRefreshToken(request);
    if (refreshToken == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh токен не найден");
    }

    String deviceId = CookieUtil.getDeviceId(request);
    if (deviceId == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "DeviceId не найден");
    }

    Account account = refreshTokenService.validateToken(refreshToken, deviceId).getAccount();

    String userAgent = request.getHeader("User-Agent");
    String ipAddress = request.getRemoteAddr();

    String newRefreshToken = refreshTokenService
        .rotateRefreshToken(refreshToken, deviceId, userAgent, ipAddress);

    String newAccessToken = jwtService.generateAccessToken(account);

    log.info("Обновлён access токен для accountId={}, deviceId={}", account.getId(), deviceId);

    return new AuthTokensDto(newAccessToken, newRefreshToken);
  }

  @Transactional
  public void logout(HttpServletRequest request) {
    String refreshToken = CookieUtil.getRefreshToken(request);
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

    int currentVersion = account.getTokenVersion() != null ? account.getTokenVersion() : 1;
    int newVersion = currentVersion + 1;
    account.setTokenVersion(newVersion);

    accountDao.update(account);

    log.info(
        "Повышена версия токена для accountId={}: {} → {}",
        accountId,
        currentVersion,
        newVersion);
  }
}
