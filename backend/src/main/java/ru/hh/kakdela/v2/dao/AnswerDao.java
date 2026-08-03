package ru.hh.kakdela.v2.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import ru.hh.kakdela.v2.model.Answer;

public interface AnswerDao {

  Optional<Answer> findByResponseIdAndQuestion(UUID responseId, UUID questionId);

  List<Answer> findAllByResponseId(UUID responseId);

  void save(Answer answer);

  void update(Answer answer);

  void delete(Answer answer);
}
