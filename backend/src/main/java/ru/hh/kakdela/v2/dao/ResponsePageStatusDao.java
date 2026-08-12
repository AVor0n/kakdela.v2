package ru.hh.kakdela.v2.dao;

import java.util.Optional;
import java.util.UUID;
import ru.hh.kakdela.v2.model.ResponsePageStatus;

public interface ResponsePageStatusDao {

  Optional<ResponsePageStatus> findResponsePageStatusByResponseIdAndPageId(
      UUID responseId, UUID pageId);

  void resetResponsePageStatusForPagesAfterSpecifiedByResponseIdAndPageId(
      UUID responseId, UUID pageId);

  void save(ResponsePageStatus responsePageStatus);

  void update(ResponsePageStatus responsePageStatus);

  void delete(ResponsePageStatus responsePageStatus);
}
