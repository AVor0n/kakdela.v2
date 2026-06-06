package ru.hh.kakdela_v2.service;

import lombok.RequiredArgsConstructor;
import ru.hh.kakdela_v2.dto.AccountCreateDto;
import ru.hh.kakdela_v2.dto.AccountResponseDto;
import ru.hh.kakdela_v2.dto.AccountUpdateDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hh.kakdela_v2.dao.AccountDao;
import ru.hh.kakdela_v2.model.Account;
import ru.hh.kakdela_v2.util.TransactionHelper;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class AccountService {

  private final AccountDao accountDao;
  private final TransactionHelper transactionHelper;

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

      Account account = Account.builder()
              .login(dto.getLogin())
              .email(dto.getEmail())
              .passwordHash(dto.getHashPassword())
              .build();

      accountDao.save(account);
      return new AccountResponseDto(account);
    });
  }

  public AccountResponseDto update(UUID id, AccountUpdateDto dto) {
    return transactionHelper.inTransaction(() -> {
      Account account = accountDao.findById(id)
              .orElseThrow(() -> new RuntimeException("Аккаунт не найден: id=" + id));

      if (dto.getLogin() != null) {
        if (accountDao.existsByLogin(dto.getLogin())) {
          throw new RuntimeException("Такой логин уже используется: " + dto.getLogin());
        }
        account.setLogin(dto.getLogin());
      }
      if (dto.getEmail() != null) {
        if (accountDao.existsByEmail(dto.getEmail())) {
          throw new RuntimeException("Такой email уже зарегистрирован: " + dto.getEmail());
        }
        account.setEmail(dto.getEmail());
      }
      if (dto.getHashPassword() != null) account.setPasswordHash(dto.getHashPassword());

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