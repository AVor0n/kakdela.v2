package ru.hh.kakdela.v2.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import ru.hh.kakdela.v2.model.SurveyPage;

public interface SurveyPageDao {

  Optional<SurveyPage> findById(UUID id);

  Optional<SurveyPage> findByIdWithAllConditionsAndParentSurveyWithRelatives(UUID id);

  List<SurveyPage> findAllBySurveyId(UUID surveyId);

  void save(SurveyPage page);

  void update(SurveyPage page);

  void delete(SurveyPage page);

  void increaseSerialNumbers(UUID surveyId, int startSerial);

  void increaseSerialNumbers(UUID surveyId, int startSerial, int endSerial);

  void decreaseSerialNumbers(UUID surveyId, int startSerial);

  void decreaseSerialNumbers(UUID surveyId, int startSerial, int endSerial);

  int findMaxSerialNumber(UUID surveyId);
}
