package ru.hh.kakdela_v2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela_v2.dao.AccountDao;
import ru.hh.kakdela_v2.dao.SurveyDao;
import ru.hh.kakdela_v2.dto.survey.SurveyCreateDto;
import ru.hh.kakdela_v2.dto.survey.SurveyResponseDto;
import ru.hh.kakdela_v2.dto.survey.SurveyShortResponseDto;
import ru.hh.kakdela_v2.dto.survey.SurveyUpdateDto;
import ru.hh.kakdela_v2.model.Account;
import ru.hh.kakdela_v2.model.Permission.SurveyRole;
import ru.hh.kakdela_v2.model.Survey;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SurveyService {

  private final SurveyDao surveyDao;
  private final AccountDao accountDao;

  @Transactional(readOnly = true)
  public SurveyResponseDto getById(UUID id) {
    Survey survey = surveyDao.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Опрос не найден: " + id));
    return new SurveyResponseDto(survey);
  }

  @Transactional(readOnly = true)
  public List<SurveyShortResponseDto> getAllByAuthorId(UUID authorId) {
    return surveyDao.findAllByAuthorId(authorId).stream()
            .map(SurveyShortResponseDto::new)
            .toList();
  }

  @Transactional(readOnly = true)
  public List<SurveyShortResponseDto> getAllPublished() {
    return surveyDao.findAllPublished().stream()
            .map(SurveyShortResponseDto::new)
            .toList();
  }

  @Transactional
  public SurveyResponseDto create(UUID authorId, SurveyCreateDto dto) {
    Account author = accountDao.findById(authorId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Аккаунт не найден: " + authorId));

    Survey survey = Survey.builder()
            .author(author)
            .title(dto.getTitle())
            .description(dto.getDescription())
            .isAuthorizedOnly(dto.isAuthorizedOnly())
            .isLimitedToOneResponse(dto.isLimitedToOneResponse())
            .isPublished(false)
            .isTemplate(false)
            .doNotify(dto.isDoNotify())
            .expireAt(dto.getExpireAt())
            .build();

    surveyDao.save(survey);
    return new SurveyResponseDto(survey);
  }

  @Transactional
  public SurveyResponseDto update(UUID id, SurveyUpdateDto dto) {
    Survey survey = surveyDao.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Опрос не найден: " + id));

    if (dto.getTitle() != null) survey.setTitle(dto.getTitle());
    if (dto.getDescription() != null) survey.setDescription(dto.getDescription());
    if (dto.getAuthorizedOnly() != null) survey.setAuthorizedOnly(dto.getAuthorizedOnly());
    if (dto.getLimitedToOneResponse() != null) survey.setLimitedToOneResponse(dto.getLimitedToOneResponse());
    if (dto.getPublished() != null) survey.setPublished(dto.getPublished());
    if (dto.getDoNotify() != null) survey.setDoNotify(dto.getDoNotify());
    if (dto.getExpireAt() != null) survey.setExpireAt(dto.getExpireAt());

    surveyDao.update(survey);
    return new SurveyResponseDto(survey);
  }

  @Transactional
  public void delete(UUID id) {
    surveyDao.delete(id);
  }
}
