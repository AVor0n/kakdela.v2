package ru.hh.kakdela.v2.dto.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.hh.kakdela.v2.exception.ErrorCode;

@AllArgsConstructor
@Getter
@Schema(name = "Error.Response")

public class ErrorResponse {
  private final LocalDateTime timestamp;
  private final ErrorCode internalErrorCode;
  private final UUID errorId;
  private final String message;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private final UUID object1Id;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private final UUID object2Id;
  private final String path;
}
