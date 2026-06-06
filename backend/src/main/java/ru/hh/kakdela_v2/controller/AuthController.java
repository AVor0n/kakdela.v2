package ru.hh.kakdela_v2.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.hh.kakdela_v2.dto.LoginDto;
import ru.hh.kakdela_v2.dto.LoginResponseDto;
import ru.hh.kakdela_v2.dto.RegisterDto;
import ru.hh.kakdela_v2.service.AuthService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  @PostMapping("/register")
  public String register(@RequestBody RegisterDto registerDto) {
    try {
      authService.register(registerDto);
    } catch (RuntimeException e) {
      return e.getMessage();
    }

    return "Пользователь успешно зарегистрирован";
  }

  @PostMapping("/login")
  public LoginResponseDto login(@RequestBody LoginDto loginDto) {
    return authService.login(loginDto);
  }
}
