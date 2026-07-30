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

    @Schema(description = "URL для доступа к файлу")
    private final String url;

    @Schema(description = "Имя файла")
    private final String fileName;

    @Schema(description = "Тип файла")
    private final String contentType;

    @Schema(description = "Размер файла в байтах")
    private final Long fileSize;
}
