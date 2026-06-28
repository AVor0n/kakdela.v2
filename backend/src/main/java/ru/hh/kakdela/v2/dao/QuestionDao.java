package ru.hh.kakdela.v2.dao;

import ru.hh.kakdela.v2.model.Question;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuestionDao {

  Optional<Question> findById(UUID id);

  List<Question> findAllByPageId(UUID pageId);

  void save(Question question);

  void update(Question question);

  void delete(Question question);

  boolean existsByPageIdAndSerialNumber(UUID pageId, Integer serialNumber);

  void shiftSerialNumbersUp(UUID pageId, int startSerial, int shift);

  void shiftSerialNumbersDown(UUID pageId, int startSerial, int shift);
}
