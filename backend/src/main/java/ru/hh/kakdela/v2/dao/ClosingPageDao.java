package ru.hh.kakdela.v2.dao;

import java.util.Optional;
import java.util.UUID;
import ru.hh.kakdela.v2.model.ClosingPage;

public interface ClosingPageDao {

  Optional<ClosingPage> findBySurveyId(UUID surveyId);

  void save(ClosingPage closingPage);

  void update(ClosingPage closingPage);

  void delete(ClosingPage closingPage);

  boolean existsBySurveyId(UUID surveyId);
}
