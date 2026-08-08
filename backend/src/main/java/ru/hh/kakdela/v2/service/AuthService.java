package ru.hh.kakdela.v2.service;

import java.security.SecureRandom;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela.v2.dao.AccountDao;
import ru.hh.kakdela.v2.dto.auth.AuthTokensDto;
import ru.hh.kakdela.v2.dto.auth.LoginDto;
import ru.hh.kakdela.v2.dto.auth.PasswordResetDto;
import ru.hh.kakdela.v2.dto.auth.VerifyCodeRequestDto;
import ru.hh.kakdela.v2.model.Account;
import ru.hh.kakdela.v2.security.JwtService;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {

  private final ObjectProvider<AuthenticationManager> authenticationManagerProvider;
  private final PasswordEncoder passwordEncoder;
  private final AccountDao accountDao;
  private final NotificationService notificationService;
  private final VerificationCodeService verificationCodeService;
  private final JwtService jwtService;
  private final RefreshTokenService refreshTokenService;

  @Transactional
  public AuthTokensDto issueTokens(
      Account account,
      String deviceId,
      String userAgent,
      String ipAddress
  ) {

    refreshTokenService.revokeAllByAccountIdAndDeviceId(account.getId(), deviceId);

    String refreshToken = refreshTokenService.createRefreshToken(
        account.getId(),
        deviceId,
        userAgent,
        ipAddress);

    String accessToken = jwtService.generateAccessToken(account);

    log.info("Успешный вход: accountId={}, deviceId={}", account.getId(), deviceId);

    return new AuthTokensDto(accessToken, refreshToken);
  }

  @Transactional
  public AuthTokensDto login(
      LoginDto loginDto,
      String deviceId,
      String userAgent,
      String ipAddress) {

    Account account = accountDao.findByLogin(loginDto.getLogin()).orElseThrow(() ->
        new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный логин или пароль"));

    authenticationManagerProvider.getObject().authenticate(
        new UsernamePasswordAuthenticationToken(loginDto.getLogin(), loginDto.getPassword()));

    log.info("Успешная аутентификация: login={}", loginDto.getLogin());

    return issueTokens(
        account,
        deviceId,
        userAgent,
        ipAddress
    );
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

  @Transactional(readOnly = true)
  public void sendPasswordResetEmail(String email) {
    Account account = accountDao.findByEmail(email).orElseThrow(() ->
        new ResponseStatusException(HttpStatus.NOT_FOUND, "Аккаунт не найден: " + email));
    if (account.getIsDeleted()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Данный аккаунт удален"
      );
    }

    String code = generateNumericCode(6);
    // сохраняет в редис
    verificationCodeService.saveVerificationCode(email, code);

    notificationService.sendPasswordResetCodeEmail(email, code);
  }

  public void verifyResetCode(VerifyCodeRequestDto dto) {
    verificationCodeService.verifyCode(dto.getEmail(), dto.getCode());
  }

  @Transactional
  public void resetPassword(PasswordResetDto dto) {
    if (!verificationCodeService
        .verifyAndDelete(dto.getEmail(), dto.getCode())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Код подтверждения неверный или истек");
    }
    if (!dto.getNewPassword().equals(dto.getPasswordConfirmation())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Пароли не совпадают");
    }

    Account account = accountDao.findByEmail(dto.getEmail()).orElseThrow(() ->
        new ResponseStatusException(HttpStatus.NOT_FOUND, "Аккаунт не найден: " + dto.getEmail()));
    if (account.getIsDeleted()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Данный аккаунт удален"
      );
    }

    logoutEverywhere(account.getId());

    account.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
    accountDao.update(account);
  }

  private static String generateNumericCode(int codeLength) {
    SecureRandom secureRandom = new SecureRandom();
    String digits = "0123456789";

    StringBuilder code = new StringBuilder(codeLength);
    for (int i = 0; i < codeLength; i++) {
      code.append(digits.charAt(secureRandom.nextInt(digits.length())));
    }

    return code.toString();
  }
}
