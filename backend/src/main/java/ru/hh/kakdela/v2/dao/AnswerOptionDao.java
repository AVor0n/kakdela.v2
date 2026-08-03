package ru.hh.kakdela.v2.dao;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import ru.hh.kakdela.v2.model.AnswerOption;

public interface AnswerOptionDao {

  Optional<AnswerOption> findById(UUID id);

  List<AnswerOption> findByIds(Set<UUID> ids);

  List<AnswerOption> findAllByQuestionId(UUID questionId);

  void save(AnswerOption option);

  void update(AnswerOption option);

  void delete(AnswerOption option);

  void increaseSerialNumbers(UUID questionId, int startSerial);

  void increaseSerialNumbers(UUID questionId, int startSerial, int endSerial);

  void decreaseSerialNumbers(UUID questionId, int startSerial);

  void decreaseSerialNumbers(UUID questionId, int startSerial, int endSerial);

  int findMaxSerialNumber(UUID questionId);
}
