package ru.hh.kakdela_v2.service;

import ru.hh.kakdela_v2.dao.SurveyDao;
import ru.hh.kakdela_v2.dao.SurveyPageDao;
import ru.hh.kakdela_v2.dto.survey_page.SurveyPageCreateDto;
import ru.hh.kakdela_v2.dto.survey_page.SurveyPageResponseDto;
import ru.hh.kakdela_v2.dto.survey_page.SurveyPageUpdateDto;
import ru.hh.kakdela_v2.model.Survey;
import ru.hh.kakdela_v2.model.SurveyPage;
import ru.hh.kakdela_v2.util.TransactionHelper;

import java.util.List;
import java.util.UUID;

public class SurveyPageService {

  private final SurveyPageDao surveyPageDao;
  private final SurveyDao surveyDao;
  private final TransactionHelper transactionHelper;

  public SurveyPageService(SurveyPageDao surveyPageDao, SurveyDao surveyDao, TransactionHelper transactionHelper) {
    this.surveyPageDao = surveyPageDao;
    this.surveyDao = surveyDao;
    this.transactionHelper = transactionHelper;
  }

  public SurveyPageResponseDto getById(UUID id) {
    return transactionHelper.inTransaction(() -> {
      SurveyPage page = surveyPageDao.findById(id)
              .orElseThrow(() -> new RuntimeException("Страница не найдена: " + id));
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

  public SurveyPageResponseDto create(UUID surveyId, SurveyPageCreateDto dto) {
    return transactionHelper.inTransaction(() -> {
      Survey survey = surveyDao.findById(surveyId)
              .orElseThrow(() -> new RuntimeException("Опрос не найден: " + surveyId));

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

  public SurveyPageResponseDto update(UUID id, SurveyPageUpdateDto dto) {
    return transactionHelper.inTransaction(() -> {
      SurveyPage page = surveyPageDao.findById(id)
              .orElseThrow(() -> new RuntimeException("Страница не найдена: " + id));

      if (dto.getSerialNumber() != null) page.setSerialNumber(dto.getSerialNumber());
      if (dto.getTitle() != null) page.setTitle(dto.getTitle());
      if (dto.getDescription() != null) page.setDescription(dto.getDescription());

      surveyPageDao.update(page);
      return new SurveyPageResponseDto(page);
    });
  }

  public void delete(UUID id) {
    transactionHelper.inTransaction(() -> {
      surveyPageDao.delete(id);
    });
  }
}
