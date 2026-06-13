package ru.hh.kakdela_v2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela_v2.dto.account.AccountCreateDto;
import ru.hh.kakdela_v2.dto.auth.LoginDto;
import ru.hh.kakdela_v2.dto.auth.LoginResponseDto;
import ru.hh.kakdela_v2.dto.auth.RegisterDto;
import ru.hh.kakdela_v2.util.JwtUtil;

@RequiredArgsConstructor
@Service
public class AuthService {

  private final AccountService accountService;
  private final UserDetailsService userDetailsService;
  private final AuthenticationManager authenticationManager;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;

  public LoginResponseDto login(LoginDto loginDto) {
    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDto.getLogin(), loginDto.getPassword()));
    UserDetails userDetails = userDetailsService.loadUserByUsername(loginDto.getLogin());
    return new LoginResponseDto(jwtUtil.generateAccessToken(userDetails), null);
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
  }
}

