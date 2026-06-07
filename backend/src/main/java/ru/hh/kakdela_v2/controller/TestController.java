package ru.hh.kakdela_v2.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class TestController {

  @GetMapping("/hello")
  public ResponseEntity<?> hello() {
    return ResponseEntity.ok("Hello! Kak dela?");
  }
}
