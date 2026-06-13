package ru.hh.kakdela_v2.dto.survey_page;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class SurveyPageUpdateDto {

  private Integer serialNumber;
  private String title;
  private String description;
}
