package ru.hh.kakdela.v2.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela.v2.dao.AccountDao;
import ru.hh.kakdela.v2.dto.account.AccountCreateDto;
import ru.hh.kakdela.v2.dto.account.AccountDeleteDto;
import ru.hh.kakdela.v2.dto.account.AccountPatchDto;
import ru.hh.kakdela.v2.dto.account.AccountPutDto;
import ru.hh.kakdela.v2.dto.account.AccountResponseDto;
import ru.hh.kakdela.v2.dto.account.HhLinkConfirmDto;
import ru.hh.kakdela.v2.dto.account.HhLinkConfirmResultDto;
import ru.hh.kakdela.v2.dto.auth.AuthTokensDto;
import ru.hh.kakdela.v2.mapper.AccountMapper;
import ru.hh.kakdela.v2.model.Account;
import ru.hh.kakdela.v2.security.CustomUserDetails;
import ru.hh.kakdela.v2.security.HhLinkTokenPayload;
import ru.hh.kakdela.v2.security.JwtService;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

  private final AccountDao accountDao;
  private final AuthService authService;
  private final PasswordEncoder passwordEncoder;
  private final RefreshTokenService refreshTokenService;
  private final Clock clock;
  private final JwtService jwtService;
  private final AuthCookieService authCookieService;

  @Transactional(readOnly = true)
  public AccountResponseDto getById(UUID id) {
    Account account = accountDao.findById(id)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Аккаунт не найден: " + id));
    if (account.getIsDeleted() != null && account.getIsDeleted()) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "Аккаунт не найден: " + id);
    }
    return AccountMapper.accountToDto(account);
  }

  @Transactional
  public AccountResponseDto create(AccountCreateDto accountCreateDto) {
    if (accountDao.existsByLogin(accountCreateDto.getLogin())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT,
          "Такой логин уже используется: " + accountCreateDto.getLogin());
    }
    if (accountDao.existsByEmail(accountCreateDto.getEmail())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT,
          "Такой email уже зарегистрирован: " + accountCreateDto.getEmail());
    }
    if (!accountCreateDto.getPassword().equals(accountCreateDto.getPasswordConfirmation())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Пароли не совпадают");
    }

    Account account = Account.builder()
        .id(UUID.randomUUID())
        .login(accountCreateDto.getLogin())
        .email(accountCreateDto.getEmail())
        .passwordHash(
            passwordEncoder.encode(accountCreateDto.getPassword()))
        .registeredAt(Instant.now(clock))
        .tokenVersion(1)
        .isDeleted(false)
        .build();

    accountDao.save(account);
    log.info("Создан аккаунт id={} login={}", account.getId(), account.getLogin());
    return AccountMapper.accountToDto(account);
  }

  @Transactional(readOnly = true)
  public Optional<Account> findByHhUserId(String hhUserId) {
    return accountDao.findByHhUserId(hhUserId);
  }

  @Transactional(readOnly = true)
  public boolean existsByEmail(String email) {
    return accountDao.existsByEmail(email);
  }

  // Возвращает существующий аккаунт, привязанный к данному пользователю hh.ru,
  // либо создает новый (с автосгенерированными login и паролем), если это первый вход
  @Transactional
  public Account findOrCreateByHhSso(String hhUserId, String email) {
    return accountDao.findByHhUserId(hhUserId)
        .map(this::requireActiveAccount)
        .orElseGet(() -> {
          if (accountDao.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Такой email уже зарегистрирован: " + email);
          }
          return createFromHhSso(hhUserId, email);
        });
  }

  private Account requireActiveAccount(Account account) {
    if (account.getIsDeleted()) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,
          "Аккаунт удалён: login=" + account.getLogin());
    }
    return account;
  }

  private Account createFromHhSso(String hhUserId, String email) {
    String login = generateUniqueLoginFromEmail(email);
    String randomPassword = UUID.randomUUID().toString();

    Account account = Account.builder()
        .id(UUID.randomUUID())
        .login(login)
        .email(email)
        .passwordHash(passwordEncoder.encode(randomPassword))
        .hhUserId(hhUserId)
        .tokenVersion(1)
        .isDeleted(false)
        .registeredAt(Instant.now(clock))
        .build();

    accountDao.save(account);
    log.info("Создан аккаунт через hh.ru SSO id={} login={}", account.getId(), account.getLogin());
    return account;
  }

  @Transactional
  public void linkHhSso(Account account, String hhUserId) {
    if (accountDao.findByHhUserId(hhUserId).isPresent()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT,
          "Этот аккаунт hh.ru уже привязан к другому пользователю");
    }

    requireActiveAccount(account);

    if (account.isHhSso()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Аккаунт уже привязан к hh.ru");
    }

    account.setHhUserId(hhUserId);
    accountDao.update(account);
    log.info("Аккаунт id={} привязан к hh.ru", account.getId());
  }

  // Подтверждение привязки hh-аккаунта по hhLinkToken, выданному Oauth2LoginSuccessHandler
  // в кейсе email-конфликта. Если currentUser авторизован - просто линкуем его аккаунт.
  // Если нет - находим аккаунт по email из токена, проверяем пароль и логиним пользователя
  // (как handleLogin в success handler'е), только потом линкуем
  @Transactional
  public HhLinkConfirmResultDto confirmLinkHhSso(
      String hhLinkToken,
      HhLinkConfirmDto dto,
      UUID currentUserId,
      HttpServletRequest request,
      HttpServletResponse response) {

    HhLinkTokenPayload payload = jwtService.extractHhLinkToken(hhLinkToken);

    Account account;
    AuthTokensDto tokens = null;

    if (currentUserId != null) {
      account = accountDao.findById(currentUserId)
          .map(this::requireActiveAccount)
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
              "Аккаунт не найден: " + currentUserId));
    } else {
      if (dto.getPassword() == null || dto.getPassword().isBlank()) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "Для привязки без активной сессии нужен пароль");
      }
      account = accountDao.findByEmail(payload.email())
          .map(this::requireActiveAccount)
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
              "Аккаунт не найден: " + payload.email()));
      if (!passwordEncoder.matches(dto.getPassword(), account.getPasswordHash())) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Предоставлен неверный пароль");
      }

      String deviceId = authCookieService.getOrCreateDeviceId(request, response);
      tokens = authService.issueTokens(
          account, deviceId, request.getHeader("User-Agent"), request.getRemoteAddr());
    }

    linkHhSso(account, payload.hhUserId());

    return new HhLinkConfirmResultDto(AccountMapper.accountToDto(account), tokens);
  }

  private String generateUniqueLoginFromEmail(String email) {
    String base = email.substring(0, email.indexOf('@'))
        .replaceAll("[^a-zA-Z0-9_.]", "")
        .toLowerCase();
    if (base.isBlank()) {
      base = "user";
    }
    base = base.substring(0, Math.min(base.length(), 28));

    String candidate = base;
    int suffix = 1;
    while (accountDao.existsByLogin(candidate)) {
      String suffixStr = String.valueOf(suffix++);
      candidate = base.substring(0, Math.min(base.length(), 32 - suffixStr.length())) + suffixStr;
    }
    return candidate;
  }

  @Transactional
  public AccountResponseDto updateFull(CustomUserDetails currentUser, AccountPutDto accountPutDto) {
    authService.checkPassword(currentUser, accountPutDto.getPassword());

    Account account = accountDao.findById(currentUser.getId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "Аккаунт не найден: " + currentUser.getId()));

    if (account.getIsDeleted() != null && account.getIsDeleted()) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Аккаунт удалён");
    }

    if (!Objects.equals(accountPutDto.getLogin(), account.getLogin())) {
      if (accountDao.existsByLogin(accountPutDto.getLogin())) {
        throw new ResponseStatusException(HttpStatus.CONFLICT,
            "Такой логин уже используется: " + accountPutDto.getLogin());
      }
      account.setLogin(accountPutDto.getLogin());
    }
    if (!Objects.equals(accountPutDto.getEmail(), account.getEmail())) {
      if (accountDao.existsByEmail(accountPutDto.getEmail())) {
        throw new ResponseStatusException(HttpStatus.CONFLICT,
            "Такой email уже зарегистрирован: " + accountPutDto.getEmail());
      }
      account.setEmail(accountPutDto.getEmail());
    }

    if (!Objects.equals(accountPutDto.getNewPassword(),
        accountPutDto.getNewPasswordConfirmation())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Пароли не совпадают");
    }
    account.setPasswordHash(passwordEncoder.encode(accountPutDto.getNewPassword()));

    accountDao.update(account);
    log.info("Изменен аккаунт id={}", currentUser.getId());
    return AccountMapper.accountToDto(account);
  }

  @Transactional
  public AccountResponseDto updatePartial(CustomUserDetails currentUser,
                                          AccountPatchDto accountPatchDto) {
    authService.checkPassword(currentUser, accountPatchDto.getPassword());

    Account account = accountDao.findById(currentUser.getId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "Аккаунт не найден: " + currentUser.getId()));

    if (account.getIsDeleted() != null && account.getIsDeleted()) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Аккаунт удалён");
    }

    if (accountPatchDto.getLogin() != null
        && !accountPatchDto.getLogin().equals(account.getLogin())) {
      if (accountDao.existsByLogin(accountPatchDto.getLogin())) {
        throw new ResponseStatusException(HttpStatus.CONFLICT,
            "Такой логин уже используется: " + accountPatchDto.getLogin());
      }
      account.setLogin(accountPatchDto.getLogin());
    }
    if (accountPatchDto.getEmail() != null
        && !accountPatchDto.getEmail().equals(account.getEmail())) {
      if (accountDao.existsByEmail(accountPatchDto.getEmail())) {
        throw new ResponseStatusException(HttpStatus.CONFLICT,
            "Такой email уже зарегистрирован: " + accountPatchDto.getEmail());
      }
      account.setEmail(accountPatchDto.getEmail());
    }
    if (accountPatchDto.getNewPassword() != null) {
      if (!accountPatchDto.getNewPassword().equals(accountPatchDto.getNewPasswordConfirmation())) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Пароли не совпадают");
      }
      account.setPasswordHash(passwordEncoder.encode(accountPatchDto.getNewPassword()));
    }

    accountDao.update(account);
    return AccountMapper.accountToDto(account);
  }

  @Transactional
  public void softDelete(CustomUserDetails currentUser, AccountDeleteDto accountDeleteDto) {
    authService.checkPassword(currentUser, accountDeleteDto.getPassword());

    Account account = accountDao.findById(currentUser.getId())
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Аккаунт не найден: " + currentUser.getId()));

    if (account.getIsDeleted() != null && account.getIsDeleted()) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Аккаунт уже удалён");
    }

    account.setIsDeleted(true);

    refreshTokenService.revokeAllByAccountId(account.getId());

    authService.incrementTokenVersion(account.getId());

    accountDao.update(account);

    log.info("Аккаунт {} помечен как удалённый", currentUser.getId());
  }
}
