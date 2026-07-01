package ru.hh.kakdela.v2.service;

import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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
        .login(accountCreateDto.getLogin())
        .email(accountCreateDto.getEmail())
        .passwordHash(
            passwordEncoder.encode(accountCreateDto.getPassword()))
        .registeredAt(Instant.now())
        .build();

    accountDao.save(account);
    return AccountMapper.accountToDto(account);
  }

  @Transactional
  public AccountResponseDto updateFull(CustomUserDetails currentUser, AccountPutDto accountPutDto) {
    authService.checkPassword(currentUser, accountPutDto.getPassword());

    Account account = accountDao.findById(currentUser.getId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "Аккаунт не найден: " + currentUser.getId()));

    if (accountDao.existsByLogin(accountPutDto.getLogin())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT,
          "Такой логин уже используется: " + accountPutDto.getLogin());
    }
    if (accountDao.existsByEmail(accountPutDto.getEmail())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT,
          "Такой email уже зарегистрирован: " + accountPutDto.getEmail());
    }
    if (!accountPutDto.getNewPassword().equals(accountPutDto.getNewPasswordConfirmation())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Пароли не совпадают");
    }

    account.setLogin(accountPutDto.getLogin());
    account.setEmail(accountPutDto.getEmail());
    account.setPasswordHash(passwordEncoder.encode(accountPutDto.getNewPassword()));

    accountDao.update(account);
    return AccountMapper.accountToDto(account);
  }

  @Transactional
  public AccountResponseDto updatePartial(CustomUserDetails currentUser,
                                          AccountPatchDto accountPatchDto) {
    authService.checkPassword(currentUser, accountPatchDto.getPassword());

    Account account = accountDao.findById(currentUser.getId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "Аккаунт не найден: " + currentUser.getId()));

    if (accountPatchDto.getLogin() != null) {
      if (accountDao.existsByLogin(accountPatchDto.getLogin())) {
        throw new ResponseStatusException(HttpStatus.CONFLICT,
            "Такой логин уже используется: " + accountPatchDto.getLogin());
      }
      account.setLogin(accountPatchDto.getLogin());
    }
    if (accountPatchDto.getEmail() != null) {
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
  public void delete(CustomUserDetails currentUser, AccountDeleteDto accountDeleteDto) {
    authService.checkPassword(currentUser, accountDeleteDto.getPassword());

    Account account = accountDao.findById(currentUser.getId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "Аккаунт не найден: " + currentUser.getId()));

    accountDao.delete(account);
  }
}
