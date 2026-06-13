package ru.hh.kakdela_v2.dto.response;

import lombok.Value;

import java.util.UUID;

@Value
public class ResponseWithTokenDto {
  UUID responseId;
  String responseEditToken;
}
