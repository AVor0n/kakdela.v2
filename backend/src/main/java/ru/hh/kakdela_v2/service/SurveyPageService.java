package ru.hh.kakdela_v2.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela_v2.dao.SurveyDao;
import ru.hh.kakdela_v2.dao.SurveyPageDao;
import ru.hh.kakdela_v2.dto.survey_page.SurveyPageCreateDto;
import ru.hh.kakdela_v2.dto.survey_page.SurveyPageResponseDto;
import ru.hh.kakdela_v2.dto.survey_page.SurveyPageUpdateDto;
import ru.hh.kakdela_v2.model.Permission.SurveyRole;
import ru.hh.kakdela_v2.model.Survey;
import ru.hh.kakdela_v2.model.SurveyPage;
import ru.hh.kakdela_v2.util.MapperUtil;

@Service
@RequiredArgsConstructor
public class SurveyPageService {

  private final SurveyPageDao surveyPageDao;
  private final PermissionService permissionService;
  private final SurveyDao surveyDao;
  private final MapperUtil mapperUtil;

  @Transactional(readOnly = true)
  public SurveyPageResponseDto getById(UUID id) {
    SurveyPage surveyPage = surveyPageDao.findById(id)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Страница не найдена: " + id));
    return mapperUtil.surveyPageToDto(surveyPage);
  }

  @Transactional(readOnly = true)
  public List<SurveyPageResponseDto> getAllBySurveyId(UUID surveyId) {
    return surveyPageDao.findAllBySurveyId(surveyId).stream()
        .map(mapperUtil::surveyPageToDto)
        .toList();
  }

  @Transactional
  public SurveyPageResponseDto create(UUID surveyId, SurveyPageCreateDto dto, UUID accountId) {
    permissionService.checkAccess(surveyId, accountId, SurveyRole.EDITOR);
    if (surveyPageDao.existsBySurveyIdAndSerialNumber(surveyId, dto.getSerialNumber())) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT,
          "Страница с номером " + dto.getSerialNumber() + " уже существует в этом опросе");
    }

    Survey survey = surveyDao.findById(surveyId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Опрос не найден: " + surveyId));

    SurveyPage surveyPage = SurveyPage.builder()
        .survey(survey)
        .serialNumber(dto.getSerialNumber())
        .title(dto.getTitle())
        .description(dto.getDescription())
        .build();

    surveyPageDao.save(surveyPage);
    return mapperUtil.surveyPageToDto(surveyPage);
  }

  @Transactional
  public SurveyPageResponseDto update(UUID surveyPageId, SurveyPageUpdateDto dto, UUID accountId) {
    SurveyPage surveyPage = surveyPageDao.findById(surveyPageId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Страница не найдена: " + surveyPageId));

    permissionService.checkAccess(surveyPage.getSurvey().getId(), accountId, SurveyRole.EDITOR);

    if (dto.getSerialNumber() != null) {
      surveyPage.setSerialNumber(dto.getSerialNumber());
    }
    if (dto.getTitle() != null) {
      surveyPage.setTitle(dto.getTitle());
    }
    if (dto.getDescription() != null) {
      surveyPage.setDescription(dto.getDescription());
    }

    surveyPageDao.update(surveyPage);
    return mapperUtil.surveyPageToDto(surveyPage);
  }

  @Transactional
  public void delete(UUID id, UUID accountId) {
    SurveyPage surveyPage = surveyPageDao.findById(id)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Страница не найдена: " + id));
    permissionService.checkAccess(surveyPage.getSurvey().getId(), accountId, SurveyRole.EDITOR);
    surveyPageDao.delete(surveyPage);
  }
}
