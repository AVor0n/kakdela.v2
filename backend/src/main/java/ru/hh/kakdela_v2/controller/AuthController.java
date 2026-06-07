package ru.hh.kakdela_v2.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.hh.kakdela_v2.dto.auth.LoginDto;
import ru.hh.kakdela_v2.dto.auth.RegisterDto;
import ru.hh.kakdela_v2.service.AuthService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  @PostMapping("/register")
  public ResponseEntity<?> register(@Valid @RequestBody RegisterDto registerDto) {
    authService.register(registerDto);
    return ResponseEntity
        .status(HttpServletResponse.SC_CREATED)
        .body("Пользователь успешно зарегистрирован");
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginDto loginDto) {
    return ResponseEntity.ok(authService.login(loginDto));
  }
}
