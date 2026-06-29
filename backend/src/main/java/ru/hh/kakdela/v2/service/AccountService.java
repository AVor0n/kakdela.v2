package ru.hh.kakdela.v2.service;

import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela.v2.dao.AccountDao;
import ru.hh.kakdela.v2.dto.account.AccountCreateDto;
import ru.hh.kakdela.v2.dto.account.AccountResponseDto;
import ru.hh.kakdela.v2.dto.account.AccountUpdateDto;
import ru.hh.kakdela.v2.mapper.AccountMapper;
import ru.hh.kakdela.v2.model.Account;

@Slf4j
@RequiredArgsConstructor
@Service
public class AccountService {

  private final AccountDao accountDao;

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
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Такой логин уже используется: " + dto.getLogin());
    }
    if (accountDao.existsByEmail(dto.getEmail())) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Такой email уже зарегистрирован: " + dto.getEmail());
    }

    Account account = Account.builder()
        .login(dto.getLogin())
        .email(dto.getEmail())
        .passwordHash(dto.getHashPassword())
        .registeredAt(Instant.now())
        .build();

    accountDao.save(account);
    log.info("Создан аккаунт id={} login={}", account.getId(), account.getLogin());
    return AccountMapper.accountToDto(account);
  }

  @Transactional
  public AccountResponseDto update(UUID id, AccountUpdateDto dto) {
    Account account = accountDao.findById(id)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Аккаунт не найден: " + id));

    if (dto.getLogin() != null) {
      if (accountDao.existsByLogin(dto.getLogin())) {
        throw new ResponseStatusException(
            HttpStatus.CONFLICT, "Такой логин уже используется: " + dto.getLogin());
      }
      account.setLogin(dto.getLogin());
    }
    if (dto.getEmail() != null) {
      if (accountDao.existsByEmail(dto.getEmail())) {
        throw new ResponseStatusException(
            HttpStatus.CONFLICT, "Такой email уже зарегистрирован: " + dto.getEmail());
      }
      account.setEmail(dto.getEmail());
    }
    if (dto.getHashPassword() != null) {
      account.setPasswordHash(dto.getHashPassword());
    }

    accountDao.update(account);
    log.info("Изменен аккаунт id={}", id);
    return AccountMapper.accountToDto(account);
  }

  @Transactional
  public void delete(UUID id) {
    Account account = accountDao.findById(id)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Аккаунт не найден: " + id));
    accountDao.delete(account);
    log.info("Удален аккаунт id={}", id);
  }
}
