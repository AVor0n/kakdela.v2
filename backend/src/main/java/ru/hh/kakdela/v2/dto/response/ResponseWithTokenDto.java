package ru.hh.kakdela.v2.dto.response;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ResponseWithTokenDto {

  private final UUID id;
  private final String responseAccessToken;
}
