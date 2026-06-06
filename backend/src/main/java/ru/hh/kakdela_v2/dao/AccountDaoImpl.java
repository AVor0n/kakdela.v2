package ru.hh.kakdela_v2.dao;

import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import ru.hh.kakdela_v2.model.Account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AccountDaoImpl implements AccountDao {

  private final SessionFactory sessionFactory;

  public AccountDaoImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  private org.hibernate.Session session() {
    return sessionFactory.getCurrentSession();
  }

  @Override
  public Optional<Account> findById(UUID id) {
    return Optional.ofNullable(session().find(Account.class, id));
  }

  @Override
  public Optional<Account> findByLogin(String login) {
    return session()
            .createQuery("FROM Account a WHERE a.login = :login", Account.class)
            .setParameter("login", login)
            .uniqueResultOptional();
  }

  @Override
  public Optional<Account> findByEmail(String email) {
    return session()
            .createQuery("FROM Account a WHERE a.email = :email", Account.class)
            .setParameter("email", email)
            .uniqueResultOptional();
  }

  @Override
  public List<Account> findAll() {
    return session()
            .createQuery("FROM Account", Account.class)
            .getResultList();
  }

  @Override
  public void save(Account account) {
    session().persist(account);
  }

  @Override
  public void update(Account account) {
    session().merge(account);
  }

  @Override
  public void delete(Account account)  {
    session().remove(account);
  }

  @Override
  public boolean existsByLogin(String login) {
    return session()
            .createQuery("SELECT COUNT(a) FROM Account a WHERE a.login = :login", Long.class)
            .setParameter("login", login)
            .uniqueResultOptional()
            .map(count -> count > 0)
            .orElse(false);
  }

  @Override
  public boolean existsByEmail(String email) {
    return session()
            .createQuery("SELECT COUNT(a) FROM Account a WHERE a.email = :email", Long.class)
            .setParameter("email", email)
            .uniqueResultOptional()
            .map(count -> count > 0)
            .orElse(false);
  }
}