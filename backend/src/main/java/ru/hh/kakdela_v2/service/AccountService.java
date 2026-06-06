package ru.hh.kakdela_v2.service;

import ru.hh.kakdela_v2.dto.AccountCreateDto;
import ru.hh.kakdela_v2.dto.AccountResponseDto;
import ru.hh.kakdela_v2.dto.AccountUpdateDto;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hh.kakdela_v2.dao.AccountDao;
import ru.hh.kakdela_v2.model.Account;
import ru.hh.kakdela_v2.util.TransactionHelper;

import java.util.UUID;

public class AccountService {

  private final AccountDao accountDao;
  private final TransactionHelper transactionHelper;

  public AccountService(AccountDao accountDao, TransactionHelper transactionHelper) {
    this.accountDao = accountDao;
    this.transactionHelper = transactionHelper;
  }

  public AccountResponseDto getById(UUID id) {
    return transactionHelper.inTransaction(() -> {
      Account account = accountDao.findById(id)
              .orElseThrow(() -> new RuntimeException("Аккаунт не найден: id=" + id));
      return new AccountResponseDto(account);
    });
  }

  public AccountResponseDto create(AccountCreateDto dto) {
    return transactionHelper.inTransaction(() -> {
      if (accountDao.existsByLogin(dto.getLogin())) {
        throw new RuntimeException("Такой логин уже используется: " + dto.getLogin());
      }
      if (accountDao.existsByEmail(dto.getEmail())) {
        throw new RuntimeException("Такой email уже зарегистрирован: " + dto.getEmail());
      }
      if (!dto.getRawPassword().equals(dto.getRawPasswordConfirmation())) {
        throw new RuntimeException("Пароли не совпадают");
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
              .orElseThrow(() -> new RuntimeException("Аккаунт не найден: id=" + id));

      if (dto.getLogin() != null) account.setLogin(dto.getLogin());
      if (dto.getEmail() != null) account.setEmail(dto.getEmail());
      if (dto.getOldRawPassword() != null && dto.getNewRawPassword() != null && dto.getNewRawPasswordConfirmation() != null) {
        if (!dto.getNewRawPassword().equals(dto.getNewRawPasswordConfirmation())) {
          account.setPasswordHash(BCrypt.hashpw(dto.getNewRawPassword(), BCrypt.gensalt()));
        } else {
          throw new RuntimeException("Пароли не совпадают");
        }
      }

      accountDao.update(account);
      return new AccountResponseDto(account);
    });
  }

  public void delete(UUID id) {
    transactionHelper.inTransaction(() -> {
      Account account = accountDao.findById(id)
          .orElseThrow(() -> new RuntimeException("Аккаунт не найден: id=" + id));
      accountDao.delete(account);
    });
  }
}