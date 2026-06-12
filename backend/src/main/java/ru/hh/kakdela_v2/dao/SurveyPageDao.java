package ru.hh.kakdela_v2.dao;

import ru.hh.kakdela_v2.model.SurveyPage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SurveyPageDao {

  Optional<SurveyPage> findById(UUID id);

  List<SurveyPage> findAllBySurveyId(UUID surveyId);

  void save(SurveyPage page);

  void update(SurveyPage page);

  void delete(SurveyPage page);

  boolean existsBySurveyIdAndSerialNumber(UUID surveyId, Integer serialNumber);
}
