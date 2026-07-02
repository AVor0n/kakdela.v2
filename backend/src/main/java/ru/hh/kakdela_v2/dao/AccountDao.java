package ru.hh.kakdela_v2.dao;

import ru.hh.kakdela_v2.model.Account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountDao {

  Optional<Account> findById(UUID id);

  Optional<Account> findByLogin(String login);

  Optional<Account> findByEmail(String email);

  List<Account> findUsersWithIncompletedResponseBySurveyId(UUID surveyId);

  List<Account> findAll();

  void save(Account account);

  void update(Account account);

  void delete(Account account);

  boolean existsByLogin(String login);

  boolean existsByEmail(String email);
}
