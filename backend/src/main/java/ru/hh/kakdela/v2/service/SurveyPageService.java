package ru.hh.kakdela.v2.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela.v2.dao.ResponseDao;
import ru.hh.kakdela.v2.dao.SurveyDao;
import ru.hh.kakdela.v2.dao.SurveyPageDao;
import ru.hh.kakdela.v2.dto.survey.page.SurveyPageCreateDto;
import ru.hh.kakdela.v2.dto.survey.page.SurveyPageResponseDto;
import ru.hh.kakdela.v2.dto.survey.page.SurveyPageUpdateDto;
import ru.hh.kakdela.v2.mapper.SurveyPageMapper;
import ru.hh.kakdela.v2.model.Response;
import ru.hh.kakdela.v2.model.Survey;
import ru.hh.kakdela.v2.model.SurveyPage;

@Slf4j
@Service
@RequiredArgsConstructor
public class SurveyPageService {

  private final SurveyPageDao surveyPageDao;
  private final ResponseDao responseDao;
  private final PermissionService permissionService;
  private final SurveyDao surveyDao;
  private final SurveyPageMapper surveyPageMapper;

  @Transactional(readOnly = true)
  public SurveyPageResponseDto getPublicById(UUID id, UUID responseId) {
    SurveyPage surveyPage = surveyPageDao.findById(id)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Страница не найдена: id=" + id));

    Response response = responseDao.findById(responseId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.FORBIDDEN, "Доступ к странице запрещён"));

    if (!response.getSurvey().getId().equals(surveyPage.getSurvey().getId())
        || response.isCompleted()) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Доступ к странице запрещён");
    }

    return surveyPageMapper.surveyPageToDto(surveyPage);
  }

  @Transactional(readOnly = true)
  public SurveyPageResponseDto getById(UUID id, UUID currentUserId) {
    SurveyPage surveyPage = surveyPageDao.findById(id)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Страница не найдена: id=" + id));

    permissionService.checkHasAnyPermission(surveyPage.getSurvey().getId(), currentUserId);

    return surveyPageMapper.surveyPageToDto(surveyPage);
  }

  @Transactional(readOnly = true)
  public List<SurveyPageResponseDto> getAllBySurveyId(UUID surveyId, UUID currentUserId) {
    if (!surveyDao.existsById(surveyId)) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "Опрос не найден: id=" + surveyId);
    }

    permissionService.checkHasAnyPermission(surveyId, currentUserId);

    return surveyPageDao.findAllBySurveyId(surveyId).stream()
        .map(surveyPageMapper::surveyPageToDto)
        .toList();
  }

  @Transactional
  public SurveyPageResponseDto create(UUID surveyId, SurveyPageCreateDto dto, UUID accountId) {
    Survey survey = surveyDao.findById(surveyId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Опрос не найден: " + surveyId));

    permissionService.checkCanEdit(surveyId, accountId);

    int maxAvailableSerial = surveyPageDao.findMaxSerialNumber(surveyId) + 1;

    if (dto.getSerialNumber() != null
        && !dto.getSerialNumber().equals(maxAvailableSerial)) {
      if (dto.getSerialNumber() > maxAvailableSerial) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "Порядковый номер должен быть не больше " + maxAvailableSerial);
      }

      surveyPageDao.increaseSerialNumbers(surveyId, dto.getSerialNumber());
    }

    SurveyPage surveyPage = SurveyPage.builder()
        .id(UUID.randomUUID())
        .survey(survey)
        .serialNumber(dto.getSerialNumber() != null
            ? dto.getSerialNumber()
            : maxAvailableSerial)
        .title(dto.getTitle())
        .description(dto.getDescription())
        .build();

    surveyPageDao.save(surveyPage);
    log.info("Создана страница id={} surveyId={}", surveyPage.getId(), surveyId);
    return surveyPageMapper.surveyPageToDto(surveyPage);
  }

  @Transactional
  public SurveyPageResponseDto update(UUID surveyPageId, SurveyPageUpdateDto dto, UUID accountId) {
    SurveyPage surveyPage = surveyPageDao.findById(surveyPageId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Страница не найдена: " + surveyPageId));

    permissionService.checkCanEdit(surveyPage.getSurvey().getId(), accountId);

    UUID surveyId = surveyPage.getSurvey().getId();
    int oldSerial = surveyPage.getSerialNumber();

    if (dto.getSerialNumber() != null && !dto.getSerialNumber().equals(oldSerial)) {
      int newSerial = dto.getSerialNumber();
      int maxAvailableSerial = surveyPageDao.findMaxSerialNumber(surveyId);
      if (newSerial > maxAvailableSerial) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "Новый номер должен быть не больше " + maxAvailableSerial);
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
    log.info("Изменена страница id={}", surveyPageId);
    return surveyPageMapper.surveyPageToDto(surveyPage);
  }

  @Transactional
  public void delete(UUID id, UUID accountId) {
    SurveyPage surveyPage = surveyPageDao.findById(id)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Страница не найдена: " + id));

    permissionService.checkCanEdit(surveyPage.getSurvey().getId(), accountId);

    UUID surveyId = surveyPage.getSurvey().getId();
    int deletedSerial = surveyPage.getSerialNumber();

    surveyPageDao.delete(surveyPage);
    surveyPageDao.decreaseSerialNumbers(surveyId, deletedSerial + 1);
    log.info("Удалена страница id={}", id);
  }
}
