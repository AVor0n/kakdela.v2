package ru.hh.kakdela.v2.dto.survey;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.hh.kakdela.v2.model.Permission;
import ru.hh.kakdela.v2.model.Survey;

@AllArgsConstructor
@Getter
public class SurveyWithUserRoleDto {

  Survey survey;
  Permission.SurveyRole role;
}
