package ru.hh.kakdela.v2.dto.subscription;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.hh.kakdela.v2.constants.Messages;

@NoArgsConstructor
@Getter
@Setter
@Schema(name = "Subscription.Request")
public class SubscriptionRequestDto {

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @NotNull(message = Messages.FIELD_SHOULD_NOT_BE_EMPTY)
  private List<String> emails;
}
