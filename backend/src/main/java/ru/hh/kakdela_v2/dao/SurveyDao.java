package ru.hh.kakdela_v2.dao;

import ru.hh.kakdela_v2.model.Survey;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SurveyDao {

  Optional<Survey> findById(UUID id);

  List<Survey> findAllByAuthorId(UUID authorId);

  List<Survey> findAllPublished();

  void save(Survey survey);

  void update(Survey survey);

  void delete(UUID id);
}
