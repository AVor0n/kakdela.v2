package ru.hh.kakdela.v2.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import ru.hh.kakdela.v2.model.TemplateBookmark;

public interface TemplateBookmarkDao {

  Optional<TemplateBookmark> findByAccountIdAndTemplateId(UUID accountId, UUID templateId);

  public List<TemplateBookmark> findAllByAccountId(UUID accountId);

  boolean existsByAccountIdAndTemplateId(UUID accountId, UUID templateId);

  void save(TemplateBookmark bookmark);

  void delete(TemplateBookmark bookmark);
}
