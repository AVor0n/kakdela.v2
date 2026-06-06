package ru.hh.kakdela_v2.service;

import org.mindrot.jbcrypt.BCrypt;
import ru.hh.kakdela_v2.dao.AccountDao;
import ru.hh.kakdela_v2.dto.account.*;
import ru.hh.kakdela_v2.model.Account;
import ru.hh.kakdela_v2.util.TransactionHelper;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

public class AccountService {

  private final AccountDao accountDao;
  private final TransactionHelper transactionHelper;
  private final JwtService jwtService;

  public AccountService(AccountDao accountDao, TransactionHelper transactionHelper, JwtService jwtService) {
    this.accountDao = accountDao;
    this.transactionHelper = transactionHelper;
    this.jwtService = jwtService;
  }

  public AccountResponseDto getById(UUID id) {
    return transactionHelper.inTransaction(() -> {
      Account account = accountDao.findById(id)
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Аккаунт не найден: " + id));
      return new AccountResponseDto(account);
    });
  }

  public AccountResponseDto register(AccountCreateDto dto) {
    return transactionHelper.inTransaction(() -> {
      if (accountDao.existsByLogin(dto.getLogin())) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Такой логин уже используется: " + dto.getLogin());
      }
      if (accountDao.existsByEmail(dto.getEmail())) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Такой email уже зарегистрирован: " + dto.getEmail());
      }
      if (!dto.getRawPassword().equals(dto.getRawPasswordConfirmation())) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Пароли не совпадают");
      }

      Account account = Account.builder()
              .login(dto.getLogin())
              .email(dto.getEmail())
              .passwordHash(BCrypt.hashpw(dto.getRawPassword(), BCrypt.gensalt()))
              .build();

      accountDao.save(account);
      return new AccountResponseDto(account);
    });
  }

  public AccountResponseDto update(UUID id, AccountUpdateDto dto) {
    return transactionHelper.inTransaction(() -> {
      Account account = accountDao.findById(id)
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Аккаунт не найден: " + id));

      if (dto.getLogin() != null) account.setLogin(dto.getLogin());
      if (dto.getEmail() != null) account.setEmail(dto.getEmail());
      if (dto.getRawPassword() != null) {
        account.setPasswordHash(BCrypt.hashpw(dto.getRawPassword(), BCrypt.gensalt())); // Заглушка ?BCrypt?
      }

      accountDao.update(account);
      return new AccountResponseDto(account);
    });
  }

  public AccountTokenDto login(AccountLoginDto dto) {
    return transactionHelper.inTransaction(() -> {
      Account account = accountDao.findByLogin(dto.getLogin())
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный логин или пароль");

      if (!BCrypt.checkpw(dto.getRawPassword(), account.getPasswordHash())) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный логин или пароль");
      }

      String token = generateToken(account); // Заглушка ?JWT?
      return new AccountTokenDto(token, account.getId());
    });
  }
}
