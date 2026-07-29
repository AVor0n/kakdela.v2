package ru.hh.kakdela.v2.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import ru.hh.kakdela.v2.model.Account;

public interface AccountDao {

  Optional<Account> findById(UUID id);

  Optional<Account> findByLogin(String login);

  Optional<Account> findByEmail(String email);

  Optional<Account> findByHhUserId(String hhUserId);

  List<Account> findUsersWithIncompletedResponseBySurveyId(UUID surveyId);

  List<Account> findAll();

  void save(Account account);

  void update(Account account);

  void delete(Account account);

  boolean existsByLogin(String login);

  boolean existsByEmail(String email);
}
