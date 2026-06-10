package ru.hh.kakdela_v2.service;

import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela_v2.dao.AccountDao;
import ru.hh.kakdela_v2.dto.account.AccountCreateDto;
import ru.hh.kakdela_v2.dto.account.AccountLoginDto;
import ru.hh.kakdela_v2.dto.account.AccountResponseDto;
import ru.hh.kakdela_v2.dto.account.AccountTokenDto;
import ru.hh.kakdela_v2.dto.account.AccountUpdateDto;
import ru.hh.kakdela_v2.model.Account;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

  private final AccountDao accountDao;
  private final JwtService jwtService;

  @Transactional(readOnly = true)
  public AccountResponseDto getById(UUID id) {
    Account account = accountDao.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Аккаунт не найден: " + id));
    return new AccountResponseDto(account);
  }

  @Transactional
  public AccountResponseDto register(AccountCreateDto dto) {
    if (accountDao.existsByLogin(dto.getLogin())) {
      throw new ResponseStatusException(
              HttpStatus.CONFLICT, "Такой логин уже используется: " + dto.getLogin());
    }
    if (accountDao.existsByEmail(dto.getEmail())) {
      throw new ResponseStatusException(
              HttpStatus.CONFLICT, "Такой email уже зарегистрирован: " + dto.getEmail());
    }
    if (!dto.getRawPassword().equals(dto.getRawPasswordConfirmation())) {
      throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST, "Пароли не совпадают");
    }

    Account account = Account.builder()
            .login(dto.getLogin())
            .email(dto.getEmail())
            .passwordHash(BCrypt.hashpw(dto.getRawPassword(), BCrypt.gensalt()))
            .build();

    accountDao.save(account);
    return new AccountResponseDto(account);
  }

  @Transactional
  public AccountResponseDto update(UUID id, AccountUpdateDto dto) {
    Account account = accountDao.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Аккаунт не найден: " + id));

    if (dto.getLogin() != null) account.setLogin(dto.getLogin());
    if (dto.getEmail() != null) account.setEmail(dto.getEmail());
    if (dto.getRawPassword() != null) {
      account.setPasswordHash(BCrypt.hashpw(dto.getRawPassword(), BCrypt.gensalt()));
    }

    accountDao.update(account);
    return new AccountResponseDto(account);
  }

  @Transactional
  public AccountTokenDto login(AccountLoginDto dto) {
    Account account = accountDao.findByLogin(dto.getLogin())
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Неверный логин или пароль"));

    if (!BCrypt.checkpw(dto.getRawPassword(), account.getPasswordHash())) {
      throw new ResponseStatusException(
              HttpStatus.UNAUTHORIZED, "Неверный логин или пароль");
    }

    String token = jwtService.generateToken(account.getId(), account.getLogin());
    return new AccountTokenDto(token, account.getId());
  }
}
