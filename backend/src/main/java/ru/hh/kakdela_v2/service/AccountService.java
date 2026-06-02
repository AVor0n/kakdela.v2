package ru.hh.kakdela_v2.service;

import ru.hh.kakdela_v2.dao.AccountDao;
import ru.hh.kakdela_v2.dto.AccountCreateDto;
import ru.hh.kakdela_v2.dto.AccountResponseDto;
import ru.hh.kakdela_v2.dto.AccountUpdateDto;
import ru.hh.kakdela_v2.dto.AccountLoginDto;
import ru.hh.kakdela_v2.dto.AccountTokenDto;
import ru.hh.kakdela_v2.model.Account;

import java.util.UUID;

import org.mindrot.jbcrypt.BCrypt;

public class AccountService {

  private final AccountDao accountDao;

  public AccountService(AccountDao accountDao) {
    this.accountDao = accountDao;
  }

  public AccountResponseDto getById(UUID id) {
    Account account = accountDao.findById(id)
            .orElseThrow(() -> new RuntimeException("Аккаунт не найден: " + id));
    return new AccountResponseDto(account);
  }

  public AccountResponseDto register(AccountCreateDto dto) {
    if (accountDao.existsByLogin(dto.getLogin())) {
      throw new RuntimeException("Такой логин уже используется: " + dto.getLogin());
    }
    if (accountDao.existsByEmail(dto.getEmail())) {
      throw new RuntimeException("Такой email уже зарегестрирован: " + dto.getEmail());
    }

    Account account = Account.builder()
            .login(dto.getLogin())
            .email(dto.getEmail())
            .passwordHash(hashPassword(dto.getRawPassword()))
            .build();

    accountDao.save(account);
    return new AccountResponseDto(account);
  }

  public AccountResponseDto update(UUID id, AccountUpdateDto dto) {
    Account account = accountDao.findById(id)
            .orElseThrow(() -> new RuntimeException("Аккаунт не найден: " + id));

    if (dto.getLogin() != null) account.setLogin(dto.getLogin());
    if (dto.getEmail() != null) account.setEmail(dto.getEmail());

    accountDao.update(account);
    return new AccountResponseDto(account);
  }

  private String hashPassword(String raw) {
    return BCrypt.hashpw(raw, BCrypt.gensalt()); // Заглушка ?BCrypt?
  }

  public AccountTokenDto login(AccountLoginDto dto) {
    Account account = accountDao.findByLogin(dto.getLogin())
            .orElseThrow(() -> new RuntimeException("Неверный логин или пароль"));

    if (!BCrypt.checkpw(dto.getRawPassword(), account.getPasswordHash())) {
      throw new RuntimeException("Неверный логин или пароль");
    }

    String token = generateToken(account);  // Заглушка ?JWT?
    return new AccountTokenDto(token, account.getId());
  }
}