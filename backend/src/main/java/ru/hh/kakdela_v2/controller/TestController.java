package ru.hh.kakdela_v2.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class TestController {

  @GetMapping("/hello")
  public String hello() {
    return "Hello! Kak dela?";
  }
}
