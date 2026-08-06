package ru.hh.kakdela.v2.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela.v2.dao.ConditionDao;
import ru.hh.kakdela.v2.dao.SurveyPageDao;
import ru.hh.kakdela.v2.dto.condition.ConditionNextPageResponseDto;
import ru.hh.kakdela.v2.dto.condition.ConditionRequestDto;
import ru.hh.kakdela.v2.dto.condition.ConditionResponseDto;
import ru.hh.kakdela.v2.mapper.ConditionMapper;
import ru.hh.kakdela.v2.model.Response;
import ru.hh.kakdela.v2.model.SurveyPage;
import ru.hh.kakdela.v2.model.condition.Condition;
import ru.hh.kakdela.v2.model.condition.ConditionElsePage;

@Service
@RequiredArgsConstructor
public class ConditionService {

  private final ConditionDao conditionDao;
  private final SurveyPageDao surveyPageDao;
  private final PermissionService permissionService;
  private final ResponseService responseService;

  @Transactional(readOnly = true)
  public ConditionResponseDto getById(UUID id, UUID accountId) {
    Condition condition = getEntityById(id);

    permissionService.checkHasAnyPermission(
        condition.getSurveyPage().getSurvey().getId(), accountId);

    return ConditionMapper.conditionToDto(condition);
  }

  @Transactional(readOnly = true)
  public List<ConditionResponseDto> getAllByPageId(UUID pageId, UUID accountId) {
    SurveyPage surveyPage = surveyPageDao.findById(pageId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Страница не найдена: id=" + pageId));

    permissionService.checkHasAnyPermission(surveyPage.getSurvey().getId(), accountId);

    return conditionDao.findAllByPageId(pageId).stream()
        .map(ConditionMapper::conditionToDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public ConditionNextPageResponseDto determineNextPage(
      UUID pageId,
      UUID responseId,
      UUID accountId,
      String token
  ) {
    SurveyPage surveyPage = surveyPageDao.findById(pageId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Страница не найдена: id=" + pageId));

    Response response = responseService.loadResponseAndCheckAccess(responseId, accountId, token);

    responseService.checkMandatoryQuestionsOfPageAnswered(responseId, pageId);

    for (Condition condition : surveyPage.getConditions()) {
      if (condition.evaluate(response)) {
        return new ConditionNextPageResponseDto(condition.getNextPage().getId());
      }
    }

    return new ConditionNextPageResponseDto(surveyPage.getElsePage().getElsePage().getId());
  }

  @Transactional
  public ConditionResponseDto create(UUID pageId, ConditionRequestDto dto, UUID accountId) {
    SurveyPage surveyPage = surveyPageDao.findById(pageId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Страница не найдена: id=" + pageId));

    permissionService.checkCanEdit(surveyPage.getSurvey().getId(), accountId);

    SurveyPage nextPage = surveyPageDao.findById(dto.getNextPageId())
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Указанная страница не найдена: id=" + dto.getNextPageId()));

    if (conditionDao.existsByPageIdAndNextPageId(pageId, dto.getNextPageId())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT,
          "Для указанной страницы и указанной следующей страницы уже существует условие");
    }

    if (!nextPage.getSurvey().getId().equals(surveyPage.getSurvey().getId())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Страница не найдена: id=" + dto.getNextPageId());
    }

    if (nextPage.getSerialNumber() <= surveyPage.getSerialNumber()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Условия могут перенаправлять только вперёд");
    }

    Condition condition = Condition.builder()
        .id(UUID.randomUUID())
        .surveyPage(surveyPage)
        .nextPage(nextPage)
        .build();

    surveyPage.getConditions().add(condition);

    Set<UUID> nextPageIds = surveyPage.getConditions().stream()
        .map(c -> c.getNextPage().getId())
        .collect(Collectors.toSet());

    surveyPage.getSurvey().getPages().stream()
        .filter(p -> !nextPageIds.contains(p.getId()))
        .findFirst()
        .ifPresent(elsePage ->
            surveyPage.setElsePage(new ConditionElsePage(surveyPage, elsePage)));

    surveyPageDao.update(surveyPage);

    return ConditionMapper.conditionToDto(condition);
  }

  @Transactional
  public ConditionResponseDto update(UUID id, ConditionRequestDto dto, UUID accountId) {
    Condition condition = getEntityById(id);

    permissionService.checkCanEdit(
        condition.getSurveyPage().getSurvey().getId(), accountId);

    SurveyPage nextPage = surveyPageDao.findById(dto.getNextPageId())
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Страница не найдена: id=" + dto.getNextPageId()));

    if (!nextPage.getSurvey().getId().equals(condition.getSurveyPage().getSurvey().getId())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Страница не найдена: id=" + dto.getNextPageId());
    }

    if (nextPage.getSerialNumber() <= condition.getSurveyPage().getSerialNumber()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Условия могут перенаправлять только вперёд");
    }

    condition.setNextPage(nextPage);

    conditionDao.update(condition);

    return ConditionMapper.conditionToDto(condition);
  }

  @Transactional
  public void delete(UUID id, UUID accountId) {
    Condition condition = getEntityById(id);

    permissionService.checkCanEdit(
        condition.getSurveyPage().getSurvey().getId(), accountId);

    conditionDao.delete(condition);
  }

  // Вспомогательные методы

  Condition getEntityById(UUID id) {
    return conditionDao.findById(id)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Условие не найдено: id=" + id));
  }
}
