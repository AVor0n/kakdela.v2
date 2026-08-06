package ru.hh.kakdela.v2.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import ru.hh.kakdela.v2.model.Response;

public interface ResponseDao {

  Optional<Response> findById(UUID id);

  List<Response> findCompletedBySurveyId(UUID surveyId);

  List<Response> findAllByAccountId(UUID accountId);

  long countAllBySurveyId(UUID surveyId);

  long countIncompletedBySurveyId(UUID surveyId);

  List<Response> findIncompletedBySurveyIdAndAccountId(UUID surveyId, UUID accountId);

  boolean existsBySurveyIdAndAccountId(UUID accountId, UUID surveyId);

  boolean areAllMandatoryQuestionsAnswered(UUID responseId);

  boolean areAllMandatoryQuestionsOfPageAnswered(UUID responseId, UUID pageId);

  void save(Response response);

  void update(Response response);

  void delete(Response response);
}
