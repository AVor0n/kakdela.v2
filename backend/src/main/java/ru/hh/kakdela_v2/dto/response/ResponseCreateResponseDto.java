package ru.hh.kakdela_v2.dto.response;

import lombok.Value;
import ru.hh.kakdela_v2.model.Response;

import java.util.UUID;

@Value
public class ResponseCreateResponseDto {
  UUID responseId;
  String responseEditToken;
}
