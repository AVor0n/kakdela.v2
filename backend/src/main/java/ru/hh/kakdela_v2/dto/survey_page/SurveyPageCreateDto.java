package ru.hh.kakdela_v2.dto.survey_page;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SurveyPageCreateDto {

  private Integer serialNumber;
  private String title;
  private String description;
}
