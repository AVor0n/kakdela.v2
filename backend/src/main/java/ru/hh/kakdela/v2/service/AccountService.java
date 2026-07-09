package ru.hh.kakdela.v2.service;

import java.time.Instant;
import java.util.Objects;
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
import ru.hh.kakdela.v2.mapper.AccountMapper;
import ru.hh.kakdela.v2.model.Account;
import ru.hh.kakdela.v2.security.CustomUserDetails;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

  private final AccountDao accountDao;
  private final AuthService authService;
  private final PasswordEncoder passwordEncoder;

  @Transactional(readOnly = true)
  public AccountResponseDto getById(UUID id) {
    Account account = accountDao.findById(id)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Аккаунт не найден: " + id));
    return AccountMapper.accountToDto(account);
  }

  @Transactional
  public AccountResponseDto create(AccountCreateDto dto) {
    if (accountDao.existsByLogin(dto.getLogin())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT,
          "Такой логин уже используется: " + dto.getLogin());
    }
    if (accountDao.existsByEmail(dto.getEmail())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT,
          "Такой email уже зарегистрирован: " + dto.getEmail());
    }
    if (!dto.getPassword().equals(dto.getPasswordConfirmation())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Пароли не совпадают");
    }

    Account account = Account.builder()
        .login(dto.getLogin())
        .email(dto.getEmail())
        .passwordHash(
            passwordEncoder.encode(dto.getPassword()))
        .registeredAt(Instant.now())
        .build();

    accountDao.save(account);
    log.info("Создан аккаунт id={} login={}", account.getId(), account.getLogin());
    return AccountMapper.accountToDto(account);
  }

  @Transactional
  public AccountResponseDto updateFull(CustomUserDetails authenticatedUser, AccountPutDto dto) {
    authService.checkPassword(authenticatedUser, dto.getPassword());

    Account account = accountDao.findById(authenticatedUser.getId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "Аккаунт не найден: " + authenticatedUser.getId()));

    if (!Objects.equals(dto.getLogin(), account.getLogin())) {
      if (accountDao.existsByLogin(dto.getLogin())) {
        throw new ResponseStatusException(HttpStatus.CONFLICT,
            "Такой логин уже используется: " + dto.getLogin());
      }
      account.setLogin(dto.getLogin());
    }
    if (!Objects.equals(dto.getEmail(), account.getEmail())) {
      if (accountDao.existsByEmail(dto.getEmail())) {
        throw new ResponseStatusException(HttpStatus.CONFLICT,
            "Такой email уже зарегистрирован: " + dto.getEmail());
      }
      account.setEmail(dto.getEmail());
    }

    if (!Objects.equals(dto.getNewPassword(),
        dto.getNewPasswordConfirmation())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Пароли не совпадают");
    }
    account.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));

    accountDao.update(account);
    log.info("Изменен аккаунт id={}", authenticatedUser.getId());
    return AccountMapper.accountToDto(account);
  }

  @Transactional
  public AccountResponseDto updatePartial(CustomUserDetails authenticatedUser,
                                          AccountPatchDto dto) {
    authService.checkPassword(authenticatedUser, dto.getPassword());

    Account account = accountDao.findById(authenticatedUser.getId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "Аккаунт не найден: " + authenticatedUser.getId()));

    if (dto.getLogin() != null
        && !dto.getLogin().equals(account.getLogin())) {
      if (accountDao.existsByLogin(dto.getLogin())) {
        throw new ResponseStatusException(HttpStatus.CONFLICT,
            "Такой логин уже используется: " + dto.getLogin());
      }
      account.setLogin(dto.getLogin());
    }
    if (dto.getEmail() != null
        && !dto.getEmail().equals(account.getEmail())) {
      if (accountDao.existsByEmail(dto.getEmail())) {
        throw new ResponseStatusException(HttpStatus.CONFLICT,
            "Такой email уже зарегистрирован: " + dto.getEmail());
      }
      account.setEmail(dto.getEmail());
    }
    if (dto.getNewPassword() != null) {
      if (!dto.getNewPassword().equals(dto.getNewPasswordConfirmation())) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Пароли не совпадают");
      }
      account.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
    }

    accountDao.update(account);
    return AccountMapper.accountToDto(account);
  }

  @Transactional
  public void delete(CustomUserDetails currentUser, AccountDeleteDto dto) {
    authService.checkPassword(currentUser, dto.getPassword());

    Account account = accountDao.findById(currentUser.getId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "Аккаунт не найден: " + currentUser.getId()));

    accountDao.delete(account);
    log.info("Удален аккаунт id={}", currentUser.getId());
  }
}
