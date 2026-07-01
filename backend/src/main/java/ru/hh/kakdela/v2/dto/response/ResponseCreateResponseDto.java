package ru.hh.kakdela.v2.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class ResponseCreateResponseDto {
  private final UUID id;
}
