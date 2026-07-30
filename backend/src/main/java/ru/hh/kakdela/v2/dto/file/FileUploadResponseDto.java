package ru.hh.kakdela.v2.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(
    name = "FileUpload.Response"
)
public class FileUploadResponseDto {

  private final String url;
  private final String fileName;
}
