package ru.hh.kakdela_v2.dao;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import ru.hh.kakdela_v2.model.Account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AccountDaoImpl implements AccountDao {

  private final SessionFactory sessionFactory;

  public AccountDaoImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  private org.hibernate.Session session() {
    return sessionFactory.getCurrentSession();
  }

  private Optional<Transaction> beginTransaction() {
    Transaction tx = session().getTransaction();
    if (!tx.isActive()) {
      tx.begin();
      return Optional.of(tx);
    }
    return Optional.empty();
  }

  @Override
  public Optional<Account> findById(UUID id) {
    Optional<Transaction> tx = beginTransaction();
    try {
      Account account = session().get(Account.class, id);
      tx.ifPresent(Transaction::commit);
      return Optional.ofNullable(account);
    } catch (RuntimeException e) {
      tx.filter(Transaction::isActive).ifPresent(Transaction::rollback);
      throw e;
    }
  }

  @Override
  public Optional<Account> findByLogin(String login) {
    Optional<Transaction> tx = beginTransaction();
    try {
      Optional<Account> result = session()
              .createQuery("FROM Account a WHERE a.login = :login", Account.class)
              .setParameter("login", login)
              .uniqueResultOptional();
      tx.ifPresent(Transaction::commit);
      return result;
    } catch (RuntimeException e) {
      tx.filter(Transaction::isActive).ifPresent(Transaction::rollback);
      throw e;
    }
  }

  @Override
  public Optional<Account> findByEmail(String email) {
    Optional<Transaction> tx = beginTransaction();
    try {
      Optional<Account> result = session()
              .createQuery("FROM Account a WHERE a.email = :email", Account.class)
              .setParameter("email", email)
              .uniqueResultOptional();
      tx.ifPresent(Transaction::commit);
      return result;
    } catch (RuntimeException e) {
      tx.filter(Transaction::isActive).ifPresent(Transaction::rollback);
      throw e;
    }
  }

  @Override
  public List<Account> findAll() {
    Optional<Transaction> tx = beginTransaction();
    try {
      List<Account> accounts = session()
              .createQuery("FROM Account", Account.class)
              .list();
      tx.ifPresent(Transaction::commit);
      return accounts;
    } catch (RuntimeException e) {
      tx.filter(Transaction::isActive).ifPresent(Transaction::rollback);
      throw e;
    }
  }

  @Override
  public void save(Account account) {
    Optional<Transaction> tx = beginTransaction();
    try {
      session().persist(account);
      tx.ifPresent(Transaction::commit);
    } catch (RuntimeException e) {
      tx.filter(Transaction::isActive).ifPresent(Transaction::rollback);
      throw e;
    }
  }

  @Override
  public void update(Account account) {
    Optional<Transaction> tx = beginTransaction();
    try {
      session().merge(account);
      tx.ifPresent(Transaction::commit);
    } catch (RuntimeException e) {
      tx.filter(Transaction::isActive).ifPresent(Transaction::rollback);
      throw e;
    }
  }

  @Override
  public void delete(UUID id) {
    Optional<Transaction> tx = beginTransaction();
    try {
      Account account = session().get(Account.class, id);
      if (account != null) {
        session().remove(account);
      }
      tx.ifPresent(Transaction::commit);
    } catch (RuntimeException e) {
      tx.filter(Transaction::isActive).ifPresent(Transaction::rollback);
      throw e;
    }
  }

  @Override
  public boolean existsByLogin(String login) {
    Optional<Transaction> tx = beginTransaction();
    try {
      Long count = session()
              .createQuery("SELECT COUNT(a) FROM Account a WHERE a.login = :login", Long.class)
              .setParameter("login", login)
              .uniqueResult();
      tx.ifPresent(Transaction::commit);
      return count != null && count > 0;
    } catch (RuntimeException e) {
      tx.filter(Transaction::isActive).ifPresent(Transaction::rollback);
      throw e;
    }
  }

  @Override
  public boolean existsByEmail(String email) {
    Optional<Transaction> tx = beginTransaction();
    try {
      Long count = session()
              .createQuery("SELECT COUNT(a) FROM Account a WHERE a.email = :email", Long.class)
              .setParameter("email", email)
              .uniqueResult();
      tx.ifPresent(Transaction::commit);
      return count != null && count > 0;
    } catch (RuntimeException e) {
      tx.filter(Transaction::isActive).ifPresent(Transaction::rollback);
      throw e;
    }
  }
}