package ru.hh.kakdela.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Schema(name = "Response.CreateResponse")
public class ResponseCreateResponseDto {

  private final UUID id;
}
