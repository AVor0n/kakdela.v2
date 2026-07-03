package ru.hh.kakdela.v2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela.v2.dto.auth.AuthTokensDto;
import ru.hh.kakdela.v2.dto.auth.LoginDto;
import ru.hh.kakdela.v2.security.JwtService;

@RequiredArgsConstructor
@Service
public class AuthService {

  private final AuthenticationManager authenticationManager;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public AuthTokensDto login(LoginDto loginDto) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(loginDto.getLogin(), loginDto.getPassword()));
    return new AuthTokensDto(
        jwtService.generateAccessToken(loginDto.getLogin()), null);
  }

  public void checkPassword(UserDetails userDetails, String providedPassword) {
    if (!passwordEncoder.matches(providedPassword, userDetails.getPassword())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Предоставлен неверный пароль");
    }
  }
}

