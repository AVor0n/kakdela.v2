package ru.hh.kakdela.v2.service;

import java.time.Instant;
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
import ru.hh.kakdela.v2.dao.TemplateBookmarkDao;
import ru.hh.kakdela.v2.dto.bookmark.TemplateBookmarkResponseDto;
import ru.hh.kakdela.v2.mapper.TemplateBookmarkMapper;
import ru.hh.kakdela.v2.mapper.TemplateMapper;
import ru.hh.kakdela.v2.model.Account;
import ru.hh.kakdela.v2.model.Survey;
import ru.hh.kakdela.v2.model.TemplateBookmark;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateBookmarkService {

  private final TemplateBookmarkDao bookmarkDao;
  private final SurveyDao surveyDao;
  private final AccountDao accountDao;
  private final TemplateBookmarkMapper templateBookmarkMapper;

  @Transactional
  public void addBookmark(UUID templateId, UUID accountId) {
    Survey template = surveyDao.findById(templateId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Шаблон не найден"));

    if (!template.isTemplate() || !template.isPublished()) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "Шаблон не найден");
    }

    if (template.isAuthor(accountId)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Нельзя добавить свой шаблон в закладки");
    }

    if (bookmarkDao.existsByAccountIdAndTemplateId(accountId, templateId)) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Шаблон уже в закладках");
    }

    Account account = accountDao.findById(accountId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Аккаунт не найден"));

    TemplateBookmark bookmark = TemplateBookmark.builder()
        .id(UUID.randomUUID())
        .account(account)
        .template(template)
        .createdAt(Instant.now())
        .build();

    bookmarkDao.save(bookmark);
    log.info("Шаблон {} добавлен в закладки пользователем {}", templateId, accountId);
  }

  @Transactional
  public void delete(UUID templateId, UUID accountId) {
    TemplateBookmark bookmark = bookmarkDao
        .findByAccountIdAndTemplateId(accountId, templateId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Закладка не найдена"));

    bookmarkDao.delete(bookmark);
    log.info("Шаблон {} удалён из закладок пользователем {}", templateId, accountId);
  }

  @Transactional(readOnly = true)
  public List<TemplateBookmarkResponseDto> getMyBookmarks(UUID accountId) {
    return bookmarkDao.findAllByAccountId(accountId).stream()
        .map(templateBookmarkMapper::toTemplateBookmarkDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public boolean isBookmarked(UUID templateId, UUID accountId) {
    return bookmarkDao.existsByAccountIdAndTemplateId(accountId, templateId);
  }
}
