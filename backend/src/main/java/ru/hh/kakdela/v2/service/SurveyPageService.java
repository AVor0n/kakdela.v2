package ru.hh.kakdela.v2.service;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela.v2.dao.SurveyDao;
import ru.hh.kakdela.v2.dao.SurveyPageDao;
import ru.hh.kakdela.v2.dto.survey_page.SurveyPageCreateDto;
import ru.hh.kakdela.v2.dto.survey_page.SurveyPageResponseDto;
import ru.hh.kakdela.v2.dto.survey_page.SurveyPageUpdateDto;
import ru.hh.kakdela.v2.mapper.SurveyPageMapper;
import ru.hh.kakdela.v2.model.Permission.SurveyRole;
import ru.hh.kakdela.v2.model.Survey;
import ru.hh.kakdela.v2.model.SurveyPage;

@Service
@RequiredArgsConstructor
public class SurveyPageService {

  private final SurveyPageDao surveyPageDao;
  private final PermissionService permissionService;
  private final SurveyDao surveyDao;
  private final SurveyPageMapper surveyPageMapper;

  @Transactional(readOnly = true)
  public SurveyPageResponseDto getById(UUID id) {
    SurveyPage surveyPage = surveyPageDao.findById(id)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Страница не найдена: " + id));
    return surveyPageMapper.surveyPageToDto(surveyPage);
  }

  @Transactional(readOnly = true)
  public List<SurveyPageResponseDto> getAllBySurveyId(UUID surveyId) {
    return surveyPageDao.findAllBySurveyId(surveyId).stream()
        .map(surveyPageMapper::surveyPageToDto)
        .toList();
  }

  @Transactional
  public SurveyPageResponseDto create(UUID surveyId, SurveyPageCreateDto dto, UUID accountId) {
    permissionService.checkAccess(surveyId, accountId, SurveyRole.EDITOR);

    surveyPageDao.increaseSerialNumbers(surveyId, dto.getSerialNumber());

    Survey survey = surveyDao.findById(surveyId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Опрос не найден: " + surveyId));

    SurveyPage surveyPage = SurveyPage.builder()
        .survey(survey)
        .serialNumber(dto.getSerialNumber() != null
            ? dto.getSerialNumber()
            : surveyPageDao.findMaxSerialNumber(surveyId) + 1)
        .title(dto.getTitle())
        .description(dto.getDescription())
        .build();

    surveyPageDao.save(surveyPage);
    return surveyPageMapper.surveyPageToDto(surveyPage);
  }

  @Transactional
  public SurveyPageResponseDto update(UUID surveyPageId, SurveyPageUpdateDto dto, UUID accountId) {
    SurveyPage surveyPage = surveyPageDao.findById(surveyPageId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Страница не найдена: " + surveyPageId));

    permissionService.checkAccess(surveyPage.getSurvey().getId(), accountId, SurveyRole.EDITOR);
    UUID surveyId = surveyPage.getSurvey().getId();
    int oldSerial = surveyPage.getSerialNumber();

    if (dto.getSerialNumber() != null && !dto.getSerialNumber().equals(oldSerial)) {
      int newSerial = dto.getSerialNumber();

      int maxSerial = surveyPageDao.findMaxSerialNumber(surveyId);
      if (newSerial > maxSerial) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "Новый номер должен быть не больше" + maxSerial);
      }

      if (oldSerial > newSerial) {
        surveyPageDao.increaseSerialNumbers(surveyId, newSerial, oldSerial - 1);
      } else {
        surveyPageDao.decreaseSerialNumbers(surveyId, oldSerial + 1, newSerial);
      }

      surveyPage.setSerialNumber(newSerial);
    }

    if (dto.getTitle() != null) {
      surveyPage.setTitle(dto.getTitle());
    }
    if (dto.getDescription() != null) {
      surveyPage.setDescription(dto.getDescription());
    }

    surveyPageDao.update(surveyPage);
    return surveyPageMapper.surveyPageToDto(surveyPage);
  }

  @Transactional
  public void delete(UUID id, UUID accountId) {
    SurveyPage surveyPage = surveyPageDao.findById(id)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Страница не найдена: " + id));
    permissionService.checkAccess(surveyPage.getSurvey().getId(), accountId, SurveyRole.EDITOR);

    UUID surveyId = surveyPage.getSurvey().getId();
    int deletedSerial = surveyPage.getSerialNumber();

    surveyPageDao.delete(surveyPage);
    surveyPageDao.decreaseSerialNumbers(surveyId, deletedSerial + 1);
  }
}
