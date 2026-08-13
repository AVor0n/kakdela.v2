package ru.hh.kakdela.v2.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class ConditionService {

  private final ConditionDao conditionDao;
  private final SurveyPageDao surveyPageDao;
  private final PermissionService permissionService;
  private final ResponseService responseService;
  private final ConditionConflictService conditionConflictService;

  @Transactional(readOnly = true)
  public ConditionResponseDto getById(UUID id, UUID accountId) {
    Condition condition = getFullyInitializedEntityById(id);

    permissionService.checkHasAnyPermission(
        getParentSurveyId(id), accountId);

    return ConditionMapper.conditionToDto(condition);
  }

  @Transactional(readOnly = true)
  public List<ConditionResponseDto> getAllByPageId(UUID pageId, UUID accountId) {
    SurveyPage page = surveyPageDao.findById(pageId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Страница не найдена: id=" + pageId));

    permissionService.checkHasAnyPermission(page.getSurvey().getId(), accountId);

    return conditionDao.findAllByPageId(pageId).stream()
        .map(ConditionMapper::conditionToDto)
        .toList();
  }

  @Transactional
  public ConditionNextPageResponseDto determineNextPage(
      UUID pageId,
      UUID responseId,
      UUID accountId,
      String token
  ) {
    log.info("Начата проверка условий дла страницы: pageId={}", pageId);

    final SurveyPage page =
        surveyPageDao.findByIdWithAllConditionsAndParentSurveyWithRelatives(pageId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Страница не найдена: id=" + pageId));

    final Response response =
        responseService.getFullyInitializedEntityByIdWithOwnerAccessCheck(
            responseId, accountId, token);

    if (!responseService.isPageIncluded(response, pageId)) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Доступ к странице запрещён");
    }

    responseService.checkMandatoryQuestionsOfPageAnswered(responseId, pageId);

    List<Condition> activeConditions = page.getConditions().stream()
        .filter(Condition::getIsActive)
        .toList();

    for (Condition condition : activeConditions) {
      if (condition.evaluate(response)) {
        SurveyPage nextPage = condition.getNextPage();
        responseService.setResponsePageStatus(response, nextPage, true);

        return new ConditionNextPageResponseDto(nextPage.getId());
      }
    }

    Optional<SurveyPage> elsePage = determineElsePage(page);
    elsePage.ifPresent(p -> responseService.setResponsePageStatus(response, p, true));

    return elsePage.map(p -> new ConditionNextPageResponseDto(p.getId()))
        .orElseGet(() -> new ConditionNextPageResponseDto(null));
  }

  @Transactional
  public ConditionResponseDto create(UUID pageId, ConditionRequestDto dto, UUID accountId) {
    log.info("Начато создание условия дла страницы: pageId={}", pageId);

    SurveyPage page = surveyPageDao.findById(pageId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Страница не найдена: id=" + pageId));

    permissionService.checkCanEdit(page.getSurvey().getId(), accountId);

    SurveyPage nextPage = surveyPageDao.findById(dto.getNextPageId())
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Указанная страница не найдена: id=" + dto.getNextPageId()));

    if (conditionDao.existsByPageIdAndNextPageId(pageId, dto.getNextPageId())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT,
          "Для указанной страницы и указанной следующей страницы уже существует условие");
    }

    if (!nextPage.getSurvey().getId().equals(page.getSurvey().getId())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Страница не найдена: id=" + dto.getNextPageId());
    }

    if (nextPage.getSerialNumber() <= page.getSerialNumber()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Условия могут перенаправлять только вперёд");
    }

    Condition condition = Condition.builder()
        .id(UUID.randomUUID())
        .surveyPage(page)
        .nextPage(nextPage)
        .build();

    conditionDao.save(condition);

    return ConditionMapper.conditionToDto(condition);
  }

  @Transactional
  public ConditionResponseDto update(UUID conditionId, ConditionRequestDto dto, UUID accountId) {
    log.info("Начато изменение условия: id={}", conditionId);

    Condition condition =
        getEntityWithParentPageWithAllQuestionsAndNeighbourConditionsById(conditionId);

    permissionService.checkCanEdit(
        getParentSurveyId(conditionId), accountId);

    SurveyPage nextPage = surveyPageDao.findById(dto.getNextPageId())
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Страница не найдена: conditionId=" + dto.getNextPageId()));

    if (!nextPage.getSurvey().getId().equals(condition.getSurveyPage().getSurvey().getId())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Страница не найдена: conditionId=" + dto.getNextPageId());
    }

    if (nextPage.getSerialNumber() <= condition.getSurveyPage().getSerialNumber()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Условия могут перенаправлять только вперёд");
    }

    if (dto.getIsActive()) {
      conditionConflictService.validatePageConditions(condition.getSurveyPage());
    }

    condition.setNextPage(nextPage);
    condition.setIsActive(dto.getIsActive());

    conditionDao.update(condition);

    return ConditionMapper.conditionToDto(condition);
  }

  @Transactional
  public void delete(UUID conditionId, UUID accountId) {
    log.info("Начато удаление условия: id={}", conditionId);

    Condition condition = getEntityById(conditionId);

    permissionService.checkCanEdit(
        getParentSurveyId(conditionId), accountId);

    conditionDao.delete(condition);
  }

  // Вспомогательные методы

  Condition getEntityById(UUID id) {
    return conditionDao.findById(id)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Условие не найдено: id=" + id));
  }

  Condition getFullyInitializedEntityById(UUID id) {
    return conditionDao.findByIdWithWholeTree(id)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Условие не найдено: id=" + id));
  }

  Condition getEntityWithParentPageWithAllQuestionsAndNeighbourConditionsById(UUID id) {
    return conditionDao.findByIdWithParentPageWithAllQuestionsAndNeighbourConditions(id)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Условие не найдено: id=" + id));
  }

  Optional<SurveyPage> determineElsePage(SurveyPage surveyPage) {
    Set<UUID> nextPageIds = surveyPage.getConditions().stream()
        .filter(Condition::getIsActive)
        .map(c -> c.getNextPage().getId())
        .collect(Collectors.toSet());

    return surveyPage.getSurvey().getPages().stream()
        .filter(p -> !nextPageIds.contains(p.getId())
            && p.getSerialNumber() > surveyPage.getSerialNumber())
        .findFirst();
  }

  UUID getParentSurveyId(UUID conditionId) {
    return conditionDao.findParentSurveyIdById(conditionId);
  }

  void makeConditionsConsistent(UUID pageId, int serialNumber) {
    conditionDao.makeConditionsConsistentByPageIdAndItsSerialNumber(pageId, serialNumber);
  }

  boolean doSurveyHaveConditions(UUID surveyId) {
    return conditionDao.existsBySurveyId(surveyId);
  }
}
