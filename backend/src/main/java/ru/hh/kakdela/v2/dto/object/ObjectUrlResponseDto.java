package ru.hh.kakdela.v2.dto.object;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Schema(name = "Object.UrlResponse")
public class ObjectUrlResponseDto {

  private final String attachmentUrl;
}
