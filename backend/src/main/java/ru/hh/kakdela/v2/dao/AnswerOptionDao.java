package ru.hh.kakdela.v2.dao;

import ru.hh.kakdela.v2.model.AnswerOption;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AnswerOptionDao {

  Optional<AnswerOption> findById(UUID id);

  List<AnswerOption> findAllByQuestionId(UUID questionId);

  void save(AnswerOption option);

  void update(AnswerOption option);

  void delete(AnswerOption option);
}
