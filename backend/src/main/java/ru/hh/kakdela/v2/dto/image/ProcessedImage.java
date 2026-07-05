package ru.hh.kakdela.v2.dto.image;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Schema(
    name = "ProcessedImage",
    title = "DTO обработанного изображения"
)
public class ProcessedImage {
  private final byte[] content;
  private final String contentType;
}
