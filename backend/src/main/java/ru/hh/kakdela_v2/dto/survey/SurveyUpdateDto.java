package ru.hh.kakdela_v2.dto.survey;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
public class SurveyUpdateDto {

  private String title;
  private String description;
  private Boolean authorizedOnly;
  private Boolean limitedToOneResponse;
  private Boolean published;
  private Boolean doNotify;
  private Instant expireAt;
}
