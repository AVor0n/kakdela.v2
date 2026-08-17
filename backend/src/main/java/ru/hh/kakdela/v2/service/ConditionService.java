package ru.hh.kakdela.v2.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hh.kakdela.v2.dao.ConditionDao;
import ru.hh.kakdela.v2.dto.condition.ConditionNextPageResponseDto;
import ru.hh.kakdela.v2.dto.condition.ConditionRequestDto;
import ru.hh.kakdela.v2.dto.condition.ConditionResponseDto;
import ru.hh.kakdela.v2.exception.condition.ConditionDubbingException;
import ru.hh.kakdela.v2.exception.condition.ConditionNextPageNotFound;
import ru.hh.kakdela.v2.exception.condition.ConditionNotFoundException;
import ru.hh.kakdela.v2.exception.condition.ConditionShouldNavigateForwardException;
import ru.hh.kakdela.v2.exception.response.ResponseBranchClosedException;
import ru.hh.kakdela.v2.mapper.ConditionMapper;
import ru.hh.kakdela.v2.model.Response;
import ru.hh.kakdela.v2.model.SurveyPage;
import ru.hh.kakdela.v2.model.condition.Condition;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConditionService {

  private final ConditionDao conditionDao;
  private final ConditionToolsService conditionToolsService;
  private final SurveyPageService surveyPageService;
  private final PermissionService permissionService;
  private final ResponseService responseService;

  @Transactional(readOnly = true)
  public ConditionResponseDto getById(UUID id, UUID accountId) {
    Condition condition = getFullyInitializedEntityById(id);

    permissionService.checkHasAnyPermission(
        getParentSurveyId(id), accountId);

    return ConditionMapper.conditionToDto(condition);
  }

  @Transactional(readOnly = true)
  public List<ConditionResponseDto> getAllByPageId(UUID pageId, UUID accountId) {
    SurveyPage page = surveyPageService.getEntityById(pageId);

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
        surveyPageService.getEntityWithAllConditionsAndParentSurveyWithPagesAndQuestionsById(
            pageId);

    final Response response =
        responseService.getFullyInitializedEntityByIdWithOwnerAccessCheck(
            responseId, accountId, token);

    if (!responseService.isPageIncluded(response, pageId)) {
      throw new ResponseBranchClosedException();
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

    SurveyPage page = surveyPageService.getEntityById(pageId);

    permissionService.checkCanEdit(page.getSurvey().getId(), accountId);

    SurveyPage nextPage = surveyPageService.getOptionalById(dto.getNextPageId())
        .orElseThrow(() -> new ConditionNextPageNotFound(dto.getNextPageId()));

    if (!nextPage.getSurvey().getId().equals(page.getSurvey().getId())) {
      throw new ConditionNextPageNotFound(dto.getNextPageId());
    }

    if (nextPage.getSerialNumber() <= page.getSerialNumber()) {
      throw new ConditionShouldNavigateForwardException();
    }

    if (dto.getIsActive()) {
      checkNoDubbingCondition(pageId, dto.getNextPageId());
    }

    Condition condition = Condition.builder()
        .id(UUID.randomUUID())
        .surveyPage(page)
        .nextPage(nextPage)
        .isActive(dto.getIsActive())
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

    SurveyPage nextPage = surveyPageService.getOptionalById(dto.getNextPageId())
        .orElseThrow(() -> new ConditionNextPageNotFound(dto.getNextPageId()));

    if (!nextPage.getSurvey().getId().equals(condition.getSurveyPage().getSurvey().getId())) {
      throw new ConditionNextPageNotFound(dto.getNextPageId());
    }

    if (nextPage.getSerialNumber() <= condition.getSurveyPage().getSerialNumber()) {
      throw new ConditionShouldNavigateForwardException();
    }

    if (dto.getIsActive()) {
      checkNoDubbingCondition(condition.getSurveyPage().getId(), dto.getNextPageId());
    }

    condition.setIsActive(dto.getIsActive());

    if (dto.getIsActive()) {
      conditionToolsService.validatePageConditions(condition.getSurveyPage());
    }

    condition.setNextPage(nextPage);

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
        .orElseThrow(() -> new ConditionNotFoundException(id));
  }

  Condition getFullyInitializedEntityById(UUID id) {
    return conditionDao.findByIdWithWholeTree(id)
        .orElseThrow(() -> new ConditionNotFoundException(id));
  }

  Condition getEntityWithParentPageWithAllQuestionsAndNeighbourConditionsById(UUID id) {
    return conditionDao.findByIdWithParentPageWithAllQuestionsAndNeighbourConditions(id)
        .orElseThrow(() -> new ConditionNotFoundException(id));
  }

  void checkNoDubbingCondition(UUID pageId, UUID nextPageId) {
    conditionDao.findActiveByPageIdAndNextPageId(pageId, nextPageId)
        .ifPresent(dc -> {
          throw new ConditionDubbingException(dc.getId());
        });
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
}
