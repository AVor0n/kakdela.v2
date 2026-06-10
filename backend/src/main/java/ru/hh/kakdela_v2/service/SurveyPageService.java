package ru.hh.kakdela_v2.service;

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
import ru.hh.kakdela_v2.model.Survey;
import ru.hh.kakdela_v2.model.SurveyPage;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SurveyPageService {

  private final SurveyPageDao surveyPageDao;
  private final SurveyDao surveyDao;

  @Transactional(readOnly = true)
  public SurveyPageResponseDto getById(UUID id) {
    SurveyPage page = surveyPageDao.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Страница не найдена: " + id));
    return new SurveyPageResponseDto(page);
  }

  @Transactional(readOnly = true)
  public List<SurveyPageResponseDto> getAllBySurveyId(UUID surveyId) {
    return surveyPageDao.findAllBySurveyId(surveyId).stream()
            .map(SurveyPageResponseDto::new)
            .toList();
  }

  @Transactional
  public SurveyPageResponseDto create(UUID surveyId, SurveyPageCreateDto dto) {
    if (surveyPageDao.existsBySurveyIdAndSerialNumber(surveyId, dto.getSerialNumber())) {
      throw new ResponseStatusException(
              HttpStatus.CONFLICT,
              "Страница с номером " + dto.getSerialNumber() + " уже существует в этом опросе");
    }

    Survey survey = surveyDao.findById(surveyId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Опрос не найден: " + surveyId));

    SurveyPage page = SurveyPage.builder()
            .survey(survey)
            .serialNumber(dto.getSerialNumber())
            .title(dto.getTitle())
            .description(dto.getDescription())
            .build();

    surveyPageDao.save(page);
    return new SurveyPageResponseDto(page);
  }

  @Transactional
  public SurveyPageResponseDto update(UUID id, SurveyPageUpdateDto dto) {
    SurveyPage page = surveyPageDao.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Страница не найдена: " + id));

    if (dto.getSerialNumber() != null) page.setSerialNumber(dto.getSerialNumber());
    if (dto.getTitle() != null) page.setTitle(dto.getTitle());
    if (dto.getDescription() != null) page.setDescription(dto.getDescription());

    surveyPageDao.update(page);
    return new SurveyPageResponseDto(page);
  }

  @Transactional
  public void delete(UUID id) {
    surveyPageDao.delete(id);
  }
}
