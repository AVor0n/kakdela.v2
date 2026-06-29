package ru.hh.kakdela.v2.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import lombok.extern.slf4j.Slf4j;
import ru.hh.kakdela.v2.model.Account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
public class AccountDaoImpl implements AccountDao {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Optional<Account> findById(UUID id) {
    return Optional.ofNullable(entityManager.find(Account.class, id));
  }

  @Override
  public Optional<Account> findByLogin(String login) {
    return Optional.ofNullable(entityManager
            .createQuery("FROM Account a WHERE a.login = :login", Account.class)
            .setParameter("login", login)
            .getSingleResultOrNull());
  }

  @Override
  public Optional<Account> findByEmail(String email) {
    return Optional.ofNullable(entityManager
            .createQuery("FROM Account a WHERE a.email = :email", Account.class)
            .setParameter("email", email)
            .getSingleResultOrNull());
  }

  @Override
  public List<Account> findAll() {
    return entityManager
            .createQuery("FROM Account", Account.class)
            .getResultList();
  }

  @Override
  public void save(Account account) {
    log.debug("Сохранен аккаунт id={}", account.getId());
    entityManager.persist(account);
  }

  @Override
  public void update(Account account) {
    log.debug("Изменен аккаунт id={}", account.getId());
    entityManager.merge(account);
  }

  @Override
  public void delete(Account account) {
    log.debug("Удален аккаунт id={}", account.getId());
    entityManager.remove(account);
  }

  @Override
  public boolean existsByLogin(String login) {
    return Optional.ofNullable(entityManager
                    .createQuery("SELECT COUNT(a) FROM Account a WHERE a.login = :login", Long.class)
                    .setParameter("login", login)
                    .getSingleResultOrNull())
            .map(count -> count > 0)
            .orElse(false);
  }

  @Override
  public boolean existsByEmail(String email) {
    return Optional.ofNullable(entityManager
                    .createQuery("SELECT COUNT(a) FROM Account a WHERE a.email = :email", Long.class)
                    .setParameter("email", email)
                    .getSingleResultOrNull())
            .map(count -> count > 0)
            .orElse(false);
  }
}
