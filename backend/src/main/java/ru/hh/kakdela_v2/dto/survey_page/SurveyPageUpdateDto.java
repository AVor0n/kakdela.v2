package ru.hh.kakdela_v2.dto.survey_page;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SurveyPageUpdateDto {

  private Integer serialNumber;
  private String title;
  private String description;
}
