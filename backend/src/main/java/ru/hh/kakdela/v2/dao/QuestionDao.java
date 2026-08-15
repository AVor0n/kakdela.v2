package ru.hh.kakdela.v2.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import ru.hh.kakdela.v2.model.Question;

public interface QuestionDao {

  Optional<Question> findById(UUID id);

  List<Question> findAllByPageId(UUID pageId);

  int findParentPageSerialNumberById(UUID id);

  void save(Question question);

  void update(Question question);

  void delete(Question question);

  void increaseSerialNumbers(UUID pageId, int startSerial);

  void increaseSerialNumbers(UUID pageId, int startSerial, int endSerial);

  void decreaseSerialNumbers(UUID pageId, int startSerial);

  void decreaseSerialNumbers(UUID pageId, int startSerial, int endSerial);

  int findMaxSerialNumber(UUID pageId);
}
