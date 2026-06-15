package ru.hh.kakdela_v2.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class ResponseWithTokenDto {
  private final UUID id;
  private final String responseAccessToken;
}
