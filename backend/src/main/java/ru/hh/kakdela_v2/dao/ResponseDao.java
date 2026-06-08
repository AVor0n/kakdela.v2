package ru.hh.kakdela_v2.dao;

import ru.hh.kakdela_v2.model.Response;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResponseDao {

  Optional<Response> findById(UUID id);

  List<Response> findAllBySurveyId(UUID surveyId);

  List<Response> findAllByAccountId(UUID accountId);

  boolean existsByAccountIdAndSurveyId(UUID accountId, UUID surveyId);  // для проверки "один ответ"

  void save(Response response);

  void update(Response response);

  void delete(UUID id);
}
