package ru.hh.kakdela.v2.service;

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
import ru.hh.kakdela.v2.model.Survey;
import ru.hh.kakdela.v2.util.DataConstraintUtil;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateService {

  private final SurveyService surveyService;
  private final PermissionService permissionService;
  private final SurveyDao surveyDao;
  private final AccountDao accountDao;
  private final SurveyMapper surveyMapper;
  private final TemplateMapper templateMapper;

  @Transactional
  public TemplateResponseDto createTemplate(UUID surveyId, UUID accountId) {
    Survey source = surveyService.getEntityById(surveyId);

    if (source.isTemplate()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Нельзя создать шаблон из шаблона");
    }

    permissionService.checkCanEdit(surveyId, accountId);

    Account author = accountDao.findById(accountId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Аккаунт не найден: " + accountId));

    Survey template = surveyService.cloneSurvey(source, author, true, source.getTitle());
    log.info("Создан шаблон из опроса sourceId={} templateId={}", surveyId, template.getId());

    return templateMapper.templateToDto(template);
  }

  @Transactional
  public TemplateResponseDto copyTemplate(UUID templateId, UUID accountId) {
    Survey source = surveyDao.findById(templateId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Шаблон не найден"));

    if (!source.isTemplate()) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "Шаблон не найден");
    }

    if (!source.isPublished()) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "Шаблон не найден"
      );
    }

    if (source.isAuthor(accountId)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Это ваш шаблон. Вы уже можете его редактировать."
      );
    }

    Account account = accountDao.findById(accountId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Аккаунт не найден"));

    String newTitle = "Копия — " + source.getTitle() + " от " + source.getAuthor().getLogin();

    Survey copy = surveyService.cloneSurvey(source, account, true, newTitle);
    log.info("Создана копия шаблона templateId={} copyId={} accountId={}",
        templateId, copy.getId(), accountId);

    return templateMapper.templateToDto(copy);
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
      DataConstraintUtil.checkTitleLength(dto.getTitle());
      template.setTitle(dto.getTitle());
    }
    if (dto.getDescription() != null) {
      DataConstraintUtil.checkDescriptionLength(dto.getDescription());
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

    if (!template.isPublished() && !template.isAuthor(accountId)) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Шаблон не опубликован");
    }

    Account newAuthor = accountDao.findById(accountId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Аккаунт не найден"));

    Survey survey = surveyService.cloneSurvey(template, newAuthor, false, template.getTitle());
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
}
