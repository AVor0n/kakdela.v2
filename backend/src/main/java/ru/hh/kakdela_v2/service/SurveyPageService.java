package ru.hh.kakdela_v2.service;

import ru.hh.kakdela_v2.dao.SurveyDao;
import ru.hh.kakdela_v2.dao.SurveyPageDao;
import ru.hh.kakdela_v2.dto.survey_page.SurveyPageCreateDto;
import ru.hh.kakdela_v2.dto.survey_page.SurveyPageResponseDto;
import ru.hh.kakdela_v2.dto.survey_page.SurveyPageUpdateDto;
import ru.hh.kakdela_v2.model.Permission.SurveyRole;
import ru.hh.kakdela_v2.model.Survey;
import ru.hh.kakdela_v2.model.SurveyPage;
import ru.hh.kakdela_v2.util.TransactionHelper;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

public class SurveyPageService {

  private final SurveyPageDao surveyPageDao;
  private final PermissionService permissionService;
  private final SurveyDao surveyDao;
  private final TransactionHelper transactionHelper;

  public SurveyPageService(SurveyPageDao surveyPageDao,PermissionService permissionService, SurveyDao surveyDao, TransactionHelper transactionHelper) {
    this.surveyPageDao = surveyPageDao;
    this.permissionService = permissionService;
    this.surveyDao = surveyDao;
    this.transactionHelper = transactionHelper;
  }

  public SurveyPageResponseDto getById(UUID id) {
    return transactionHelper.inTransaction(() -> {
      SurveyPage page = surveyPageDao.findById(id)
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Страница не найдена: " + id));
      return new SurveyPageResponseDto(page);
    });
  }

  public List<SurveyPageResponseDto> getAllBySurveyId(UUID surveyId) {
    return transactionHelper.inTransaction(() ->
            surveyPageDao.findAllBySurveyId(surveyId).stream()
                    .map(SurveyPageResponseDto::new)
                    .toList()
    );
  }

  public SurveyPageResponseDto create(UUID surveyId, SurveyPageCreateDto dto, UUID userId) {
    return transactionHelper.inTransaction(() -> {
       permissionService.checkAccess(surveyId, userId, SurveyRole.EDITOR);

      Survey survey = surveyDao.findById(surveyId)
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Опрос не найден: " + surveyId));

      if (surveyPageDao.existsBySurveyIdAndSerialNumber(surveyId, dto.getSerialNumber())) {
        throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Страница с номером " + dto.getSerialNumber() + " уже существует в этом опросе"
        );
      }

      SurveyPage page = SurveyPage.builder()
              .survey(survey)
              .serialNumber(dto.getSerialNumber())
              .title(dto.getTitle())
              .description(dto.getDescription())
              .build();
      surveyPageDao.save(page);
      return new SurveyPageResponseDto(page);
    });
  }

  public SurveyPageResponseDto update(UUID id, SurveyPageUpdateDto dto, UUID userId) {
    return transactionHelper.inTransaction(() -> {
      SurveyPage page = surveyPageDao.findById(id)
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Страница не найдена: " + id));

      permissionService.checkAccess(page.getSurvey().getId(), userId, SurveyRole.EDITOR);

      if (dto.getSerialNumber() != null) page.setSerialNumber(dto.getSerialNumber());
      if (dto.getTitle() != null) page.setTitle(dto.getTitle());
      if (dto.getDescription() != null) page.setDescription(dto.getDescription());

      surveyPageDao.update(page);
      return new SurveyPageResponseDto(page);
    });
  }

  public void delete(UUID id, UUID userId) {
    transactionHelper.inTransaction(() -> {
      SurveyPage page = surveyPageDao.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Страница не найдена: " + id));

            permissionService.checkOwnership(page.getSurvey().getId(), userId);
      surveyPageDao.delete(id);
    });
  }
}
