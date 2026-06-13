package ru.hh.kakdela_v2.dao;

import ru.hh.kakdela_v2.model.Response;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResponseDao {

  Optional<Response> findById(UUID id);

  List<Response> findCompletedBySurveyId(UUID surveyId);

  List<Response> findAllByAccountId(UUID accountId);

  long countAllBySurveyId(UUID surveyId);

  long countIncompletedBySurveyId(UUID surveyId);

  List<Response> findIncompletedBySurveyIdAndAccountId(UUID surveyId, UUID accountId);

  boolean existsBySurveyIdAndAccountId(UUID accountId, UUID surveyId);

  boolean areAllMandatoryQuestionsAnswered(UUID responseId);

  void save(Response response);

  void update(Response response);

  void delete(Response response);
}
