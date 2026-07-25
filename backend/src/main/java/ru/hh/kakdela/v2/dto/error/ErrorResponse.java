package ru.hh.kakdela.v2.dto.error;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "ErrorResponse",
    title = "DTO ошибок"
)

public class ErrorResponse {
  private LocalDateTime timestamp;
  private int status;
  private String error;
  private String message;
  private String path;
  private String details;
}
