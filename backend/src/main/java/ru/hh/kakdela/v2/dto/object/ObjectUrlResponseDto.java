package ru.hh.kakdela.v2.dto.object;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Schema(
    name = "ObjectUrlResponse",
    title = "DTO URL объекта"
)
public class ObjectUrlResponseDto {
  private final String attachmentUrl;
}
