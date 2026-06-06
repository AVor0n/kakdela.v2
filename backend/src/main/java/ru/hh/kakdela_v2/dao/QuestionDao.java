package ru.hh.kakdela_v2.dao;

import ru.hh.kakdela_v2.model.Question;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuestionDao {

  Optional<Question> findById(UUID id);

  List<Question> findAllByPageId(UUID pageId);

  void save(Question question);

  void update(Question question);

  void delete(UUID id);
}
