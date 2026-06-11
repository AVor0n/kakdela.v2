package ru.hh.kakdela_v2.dao;

import ru.hh.kakdela_v2.model.Answer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AnswerDao {

  Optional<Answer> findById(Answer.AnswerId id);

  List<Answer> findAllByResponseId(UUID responseId);

  void save(Answer answer);

  void update(Answer answer);

  void delete(Answer answer);
}
