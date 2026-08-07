package ru.hh.kakdela.v2.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import ru.hh.kakdela.v2.model.Survey;

public interface SurveyDao {

  Optional<Survey> findById(UUID id);

  List<Survey> findAllByAuthorId(UUID authorId);

  List<Survey> findAllPublished();

  List<Survey> findAllPublishedTemplates();

  void save(Survey survey);

  void update(Survey survey);

  void delete(Survey survey);
}
