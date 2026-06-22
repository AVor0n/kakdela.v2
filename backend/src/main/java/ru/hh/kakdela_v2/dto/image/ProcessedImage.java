package ru.hh.kakdela_v2.dto.image;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ProcessedImage {
  private final byte[] content;
  private final String contentType;
}
