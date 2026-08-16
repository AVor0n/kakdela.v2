package ru.hh.kakdela.v2.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela.v2.dao.SurveyDao;
import ru.hh.kakdela.v2.dao.SurveyPageDao;
import ru.hh.kakdela.v2.dto.survey.page.SurveyPageCreateDto;
import ru.hh.kakdela.v2.dto.survey.page.SurveyPagePublicResponseDto;
import ru.hh.kakdela.v2.dto.survey.page.SurveyPageResponseDto;
import ru.hh.kakdela.v2.dto.survey.page.SurveyPageUpdateDto;
import ru.hh.kakdela.v2.exception.response.ResponseBranchClosedException;
import ru.hh.kakdela.v2.exception.response.ResponseNotFoundOrCompletedException;
import ru.hh.kakdela.v2.exception.survey.page.SurveyPageNotFoundException;
import ru.hh.kakdela.v2.mapper.SurveyPageMapper;
import ru.hh.kakdela.v2.model.Response;
import ru.hh.kakdela.v2.model.Survey;
import ru.hh.kakdela.v2.model.SurveyPage;
import ru.hh.kakdela.v2.util.DataConstraintUtil;

@Slf4j
@Service
@RequiredArgsConstructor
public class SurveyPageService {

  private final SurveyPageDao surveyPageDao;
  private final SurveyDao surveyDao;
  private final PermissionService permissionService;
  private final ResponseService responseService;
  private final ConditionToolsService conditionToolsService;
  private final SurveyPageMapper surveyPageMapper;

  @Transactional(readOnly = true)
  public SurveyPagePublicResponseDto getPublicById(
      UUID pageId,
      UUID responseId,
      UUID accountId,
      String token
  ) {
    SurveyPage page = getEntityById(pageId);

    Response response =
        responseService.getEntityWithPageStatusesByIdWithOwnerAccessCheck(
            responseId, accountId, token);

    if (!response.getSurvey().getId().equals(page.getSurvey().getId())
        || response.isCompleted()) {
      throw new ResponseNotFoundOrCompletedException(responseId);
    }

    if (!responseService.isPageIncluded(response, pageId)) {
      throw new ResponseBranchClosedException();
    }

    return surveyPageMapper.surveyPageToPublicDto(page);
  }

  @Transactional(readOnly = true)
  public SurveyPageResponseDto getById(UUID pageId, UUID accountId) {
    SurveyPage page = getEntityById(pageId);

    permissionService.checkHasAnyPermission(page.getSurvey().getId(), accountId);

    return surveyPageMapper.surveyPageToDto(page);
  }

  @Transactional(readOnly = true)
  public List<SurveyPageResponseDto> getAllBySurveyId(UUID surveyId, UUID accountId) {
    if (!surveyDao.existsById(surveyId)) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "Опрос не найден: id=" + surveyId);
    }

    permissionService.checkHasAnyPermission(surveyId, accountId);

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
      DataConstraintUtil.checkSerialNumberUpperLimit(dto.getSerialNumber(), maxAvailableSerial);

      surveyPageDao.increaseSerialNumbers(surveyId, dto.getSerialNumber());
    }

    DataConstraintUtil.checkTitleLength(dto.getTitle());
    DataConstraintUtil.checkDescriptionLength(dto.getDescription());

    SurveyPage page = SurveyPage.builder()
        .id(UUID.randomUUID())
        .survey(survey)
        .serialNumber(dto.getSerialNumber() != null
            ? dto.getSerialNumber()
            : maxAvailableSerial)
        .title(dto.getTitle())
        .description(dto.getDescription())
        .build();

    surveyPageDao.save(page);
    log.info("Создана страница id={} surveyId={}", page.getId(), surveyId);
    return surveyPageMapper.surveyPageToDto(page);
  }

  @Transactional
  public SurveyPageResponseDto update(UUID pageId, SurveyPageUpdateDto dto, UUID accountId) {
    SurveyPage page = getEntityById(pageId);

    permissionService.checkCanEdit(page.getSurvey().getId(), accountId);

    UUID surveyId = page.getSurvey().getId();
    int oldSerial = page.getSerialNumber();

    if (dto.getSerialNumber() != null && !dto.getSerialNumber().equals(oldSerial)) {
      int newSerial = dto.getSerialNumber();
      int maxAvailableSerial = surveyPageDao.findMaxSerialNumber(surveyId);

      DataConstraintUtil.checkSerialNumberUpperLimit(newSerial, maxAvailableSerial);

      if (oldSerial > newSerial) {
        surveyPageDao.increaseSerialNumbers(surveyId, newSerial, oldSerial - 1);
      } else {
        surveyPageDao.decreaseSerialNumbers(surveyId, oldSerial + 1, newSerial);
      }

      page.setSerialNumber(newSerial);

      conditionToolsService.makeConditionsConsistent(pageId, newSerial);
    }

    if (dto.getTitle() != null) {
      DataConstraintUtil.checkTitleLength(dto.getTitle());
      page.setTitle(dto.getTitle());
    }
    if (dto.getDescription() != null) {
      DataConstraintUtil.checkDescriptionLength(dto.getDescription());
      page.setDescription(dto.getDescription());
    }

    surveyPageDao.update(page);
    log.info("Изменена страница id={}", pageId);
    return surveyPageMapper.surveyPageToDto(page);
  }

  @Transactional
  public void delete(UUID pageId, UUID accountId) {
    SurveyPage page = getEntityById(pageId);

    permissionService.checkCanEdit(page.getSurvey().getId(), accountId);

    UUID surveyId = page.getSurvey().getId();
    int deletedSerial = page.getSerialNumber();

    surveyPageDao.delete(page);
    surveyPageDao.decreaseSerialNumbers(surveyId, deletedSerial + 1);
    log.info("Удалена страница pageId={}", pageId);
  }

  // Вспомогательные методы

  SurveyPage getEntityById(UUID id) {
    return surveyPageDao.findById(id)
        .orElseThrow(() -> new SurveyPageNotFoundException(id));
  }

  SurveyPage getEntityWithAllConditionsAndParentSurveyWithPagesAndQuestionsById(UUID id) {
    return surveyPageDao.findByIdWithAllConditionsAndParentSurveyWithPagesAndQuestions(id)
        .orElseThrow(() -> new SurveyPageNotFoundException(id));
  }

  Optional<SurveyPage> getOptionalById(UUID id) {
    return surveyPageDao.findById(id);
  }
}
