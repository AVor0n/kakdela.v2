package ru.hh.kakdela.v2.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import ru.hh.kakdela.v2.model.Survey;
import ru.hh.kakdela.v2.model.TemplateBookmark;

public interface TemplateBookmarkDao {

  Optional<TemplateBookmark> findByAccountIdAndTemplateId(UUID accountId, UUID templateId);

  List<Survey> findTemplatesByAccountId(UUID accountId);

  boolean existsByAccountIdAndTemplateId(UUID accountId, UUID templateId);

  void save(TemplateBookmark bookmark);

  void delete(TemplateBookmark bookmark);

  void deleteByAccountIdAndTemplateId(UUID accountId, UUID templateId);
}
