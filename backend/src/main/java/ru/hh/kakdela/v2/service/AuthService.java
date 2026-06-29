package ru.hh.kakdela.v2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela.v2.dto.account.AccountCreateDto;
import ru.hh.kakdela.v2.dto.auth.AuthTokensDto;
import ru.hh.kakdela.v2.dto.auth.LoginDto;
import ru.hh.kakdela.v2.dto.auth.RegisterDto;
import ru.hh.kakdela.v2.security.JwtService;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {

  private final AccountService accountService;
  private final UserDetailsService userDetailsService;
  private final AuthenticationManager authenticationManager;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public AuthTokensDto login(LoginDto loginDto) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(loginDto.getLogin(), loginDto.getPassword()));
    log.info("Выполнен вход login={}", loginDto.getLogin());
    return new AuthTokensDto(
        jwtService.generateAccessToken(loginDto.getLogin()), null);
  }

  public void register(RegisterDto registerDto) {
    if (!registerDto.getPassword().equals(registerDto.getPasswordConfirmation())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Пароли не совпадают");
    }

    AccountCreateDto accountCreateDto = new AccountCreateDto();

    accountCreateDto.setLogin(registerDto.getLogin());
    accountCreateDto.setEmail(registerDto.getEmail());
    accountCreateDto.setHashPassword(passwordEncoder.encode(registerDto.getPassword()));

    accountService.create(accountCreateDto);
    log.info("Зарегистрирован пользователь login={}", registerDto.getLogin());
  }
}
