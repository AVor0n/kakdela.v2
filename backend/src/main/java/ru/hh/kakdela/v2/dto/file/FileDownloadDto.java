package ru.hh.kakdela.v2.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(
    name = "File.Download"
)
public class FileDownloadDto {
    private final byte[] content;
    private final String fileName;
    private final String contentType;
    private final Long fileSize;
}
