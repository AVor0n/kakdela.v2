package ru.hh.kakdela_v2.dto.survey;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SurveyCreateDto {

  private String title;
  private String description;
  private boolean isAuthorizedOnly;
  private boolean isLimitedToOneResponse;
  private boolean doNotify;
  private Instant expireAt;
}
