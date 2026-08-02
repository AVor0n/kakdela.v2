package ru.hh.kakdela.v2.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@EqualsAndHashCode
@ToString  
@AllArgsConstructor
@Schema(
    name = "FileUpload.Response"
)
public class FileResponseDto {

  private final String fileName;
  private final Long fileSize;
}
