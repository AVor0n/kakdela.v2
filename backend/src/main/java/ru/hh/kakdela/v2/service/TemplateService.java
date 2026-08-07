package ru.hh.kakdela.v2.service;

import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela.v2.dao.AccountDao;
import ru.hh.kakdela.v2.dao.SurveyDao;
import ru.hh.kakdela.v2.dto.survey.SurveyResponseDto;
import ru.hh.kakdela.v2.dto.template.TemplateResponseDto;
import ru.hh.kakdela.v2.dto.template.TemplateUpdateDto;
import ru.hh.kakdela.v2.mapper.SurveyMapper;
import ru.hh.kakdela.v2.mapper.TemplateMapper;
import ru.hh.kakdela.v2.model.Account;
import ru.hh.kakdela.v2.model.AnswerOption;
import ru.hh.kakdela.v2.model.ClosingPage;
import ru.hh.kakdela.v2.model.Question;
import ru.hh.kakdela.v2.model.Survey;
import ru.hh.kakdela.v2.model.SurveyPage;
import ru.hh.kakdela.v2.service.ObjectStorageService;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateService {

  private final SurveyDao surveyDao;
  private final AccountDao accountDao;
  private final SurveyMapper surveyMapper;
  private final TemplateMapper templateMapper;
  private final PermissionService permissionService;
  private final ObjectStorageService objectStorageService;

  @Transactional
  public TemplateResponseDto createTemplate(UUID surveyId, UUID accountId) {
    Survey source = surveyDao.findById(surveyId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Опрос не найден: id=" + surveyId));

    if (source.isTemplate()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Нельзя создать шаблон из шаблона");
    }

    permissionService.checkCanEdit(surveyId, accountId);

    Account author = accountDao.findById(accountId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Аккаунт не найден: " + accountId));

    Survey template = cloneSurvey(source, author, true, source.getTitle(), false);

    surveyDao.save(template);
    log.info("Создан шаблон из опроса sourceId={} templateId={}", surveyId, template.getId());

    return templateMapper.templateToDto(template);
  }

  @Transactional(readOnly = true)
  public TemplateResponseDto getTemplate(UUID templateId, UUID accountId) {
    Survey template = surveyDao.findById(templateId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Шаблон не найден"));

    if (!template.isTemplate()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Указанный опрос не является шаблоном");
    }

    if (!template.isPublished() && !template.isAuthor(accountId)) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Нет доступа к шаблону");
    }

    return templateMapper.templateToDto(template);
  }

  @Transactional(readOnly = true)
  public List<TemplateResponseDto> getPublicTemplates() {
    return templateMapper.templatesToDtoList(
        surveyDao.findAllPublishedTemplates()
    );
  }

  @Transactional(readOnly = true)
  public List<TemplateResponseDto> getMyTemplates(UUID accountId) {
    List<Survey> templates = surveyDao.findAllByAuthorId(accountId).stream()
        .filter(Survey::isTemplate)
        .toList();
    return templateMapper.templatesToDtoList(templates);
  }

  @Transactional
  public TemplateResponseDto updateTemplate(
      UUID templateId,
      TemplateUpdateDto dto,
      UUID accountId) {

    Survey template = surveyDao.findById(templateId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Шаблон не найден"));

    if (!template.isTemplate()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Указанный опрос не является шаблоном");
    }

    if (!template.isAuthor(accountId)) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Только автор может редактировать шаблон");
    }

    if (dto.getTitle() != null) {
      template.setTitle(dto.getTitle());
    }
    if (dto.getDescription() != null) {
      template.setDescription(dto.getDescription());
    }
    if (dto.getIsPublished() != null) {
      template.setPublished(dto.getIsPublished());
    }

    surveyDao.update(template);
    log.info("Обновлён шаблон id={}", templateId);

    return templateMapper.templateToDto(template);
  }

  @Transactional
  public SurveyResponseDto createSurveyFromTemplate(UUID templateId, UUID accountId) {
    Survey template = surveyDao.findById(templateId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Шаблон не найден"));

    if (!template.isTemplate()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Указанный опрос не является шаблоном");
    }

    if (!template.isPublished()) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Шаблон не опубликован");
    }

    Account newAuthor = accountDao.findById(accountId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Аккаунт не найден"));

    Survey survey = cloneSurvey(template, newAuthor, false, template.getTitle(), false);

    surveyDao.save(survey);
    log.info("Создан опрос из шаблона templateId={} surveyId={}", templateId, survey.getId());

    return surveyMapper.surveyToDto(survey);
  }

  @Transactional
  public void deleteTemplate(UUID templateId, UUID accountId) {
    Survey template = surveyDao.findById(templateId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Шаблон не найден"));

    if (!template.isTemplate()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Указанный опрос не является шаблоном");
    }

    if (!template.isAuthor(accountId)) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Только автор может удалить шаблон");
    }

    surveyDao.delete(template);
    log.info("Удалён шаблон id={}", templateId);
  }

  private Survey cloneSurvey(Survey source, Account newAuthor, boolean asTemplate,
                             String customTitle, boolean preservePublished) {

    Account author = newAuthor != null ? newAuthor : source.getAuthor();

    Survey cloned = Survey.builder()
        .id(UUID.randomUUID())
        .author(author)
        .title(customTitle != null ? customTitle : source.getTitle())
        .description(source.getDescription())
        .isAuthorizedOnly(source.isAuthorizedOnly())
        .isLimitedToOneResponse(source.isLimitedToOneResponse())
        .doNotify(asTemplate ? false : source.doNotify())
        .expireAt(source.getExpireAt())
        .targetTimezone(source.getTargetTimezone())
        .createdAt(Instant.now().truncatedTo(ChronoUnit.SECONDS))
        .isTemplate(asTemplate)
        .isPublished(asTemplate ? false : (preservePublished ? source.isPublished() : false))
        .build();

    for (SurveyPage originalPage : source.getPages()) {
      SurveyPage pageCopy = clonePage(originalPage, cloned);
      cloned.getPages().add(pageCopy);
    }

    if (source.getClosingPage() != null) {
      ClosingPage closingPageCopy = cloneClosingPage(source.getClosingPage(), cloned);
      cloned.setClosingPage(closingPageCopy);
    }

    if (asTemplate) {
      cloned.setPermissions(new ArrayList<>());
      cloned.setResponses(new ArrayList<>());
    }

    log.debug("Клонирован опрос sourceId={} cloneId={} asTemplate={}",
        source.getId(), cloned.getId(), asTemplate);

    return cloned;
  }

  private SurveyPage clonePage(SurveyPage originalPage, Survey newSurvey) {
    SurveyPage pageCopy = SurveyPage.builder()
        .id(UUID.randomUUID())
        .survey(newSurvey)
        .serialNumber(originalPage.getSerialNumber())
        .title(originalPage.getTitle())
        .description(originalPage.getDescription())
        .build();

    for (Question originalQuestion : originalPage.getQuestions()) {
      Question questionCopy = cloneQuestion(originalQuestion, pageCopy);
      pageCopy.getQuestions().add(questionCopy);
    }

    return pageCopy;
  }

  private Question cloneQuestion(Question originalQuestion, SurveyPage newPage) {
    UUID questionId = UUID.randomUUID();

    Question questionCopy = Question.builder()
        .id(questionId)
        .surveyPage(newPage)
        .serialNumber(originalQuestion.getSerialNumber())
        .text(originalQuestion.getText())
        .description(originalQuestion.getDescription())
        .type(originalQuestion.getType())
        .answerOptionOrder(originalQuestion.getAnswerOptionOrder())
        .hasOtherOption(originalQuestion.hasOtherOption())
        .isMandatory(originalQuestion.isMandatory())
        .isVisible(originalQuestion.isVisible())
        .condition(originalQuestion.getCondition())
        .build();

    if (originalQuestion.getAttachmentObjectKey() != null) {
      String newKey = "questions/%s/%s".formatted(questionId, UUID.randomUUID());
      objectStorageService.copyObject(originalQuestion.getAttachmentObjectKey(), newKey);
      questionCopy.setAttachmentObjectKey(newKey);
    }

    for (AnswerOption originalOption : originalQuestion.getAnswerOptions()) {
      AnswerOption optionCopy = cloneAnswerOption(originalOption, questionCopy);
      questionCopy.getAnswerOptions().add(optionCopy);
    }

    return questionCopy;
  }

  private AnswerOption cloneAnswerOption(AnswerOption originalOption, Question newQuestion) {
    UUID optionId = UUID.randomUUID();

    AnswerOption optionCopy = AnswerOption.builder()
        .id(optionId)
        .question(newQuestion)
        .serialNumber(originalOption.getSerialNumber())
        .text(originalOption.getText())
        .build();

    if (originalOption.getAttachmentObjectKey() != null) {
      String newKey = "answer-options/%s/%s".formatted(optionId, UUID.randomUUID());
      objectStorageService.copyObject(originalOption.getAttachmentObjectKey(), newKey);
      optionCopy.setAttachmentObjectKey(newKey);
    }

    return optionCopy;
  }

  private ClosingPage cloneClosingPage(ClosingPage originalClosingPage, Survey newSurvey) {
    UUID closingPageId = UUID.randomUUID();

    ClosingPage closingPageCopy = ClosingPage.builder()
        .id(closingPageId)
        .survey(newSurvey)
        .title(originalClosingPage.getTitle())
        .description(originalClosingPage.getDescription())
        .websiteUrl(originalClosingPage.getWebsiteUrl())
        .build();

    if (originalClosingPage.getAttachmentObjectKey() != null) {
      String newKey = "closing-pages/%s/%s".formatted(closingPageId, UUID.randomUUID());
      objectStorageService.copyObject(originalClosingPage.getAttachmentObjectKey(), newKey);
      closingPageCopy.setAttachmentObjectKey(newKey);
    }

    if (originalClosingPage.getFileObjectKey() != null) {
      String originalKey = originalClosingPage.getFileObjectKey();
      String fileName = Paths.get(originalKey).getFileName().toString();
      String newKey = "closing-pages/%s/%s".formatted(closingPageId, fileName);
      objectStorageService.copyObject(originalKey, newKey);
      closingPageCopy.setFileObjectKey(newKey);
    }
    return closingPageCopy;
  }
}
