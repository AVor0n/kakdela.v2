package ru.hh.kakdela_v2.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import ru.hh.kakdela_v2.model.Permission;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PermissionDaoImpl implements PermissionDao {

  private final SessionFactory sessionFactory;

  public PermissionDaoImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  private Session session() {
    return sessionFactory.getCurrentSession();
  }

  @Override
  public Optional<Permission> findById(Permission.PermissionId id) {
    return Optional.ofNullable(session().find(Permission.class, id));
  }

  @Override
  public List<Permission> findAllBySurveyId(UUID surveyId) {
    return session()
            .createQuery("""
                    FROM Permission p
                    WHERE p.id.surveyId = :surveyId
                    """, Permission.class)
            .setParameter("surveyId", surveyId)
            .getResultList();
  }

  @Override
  public List<Permission> findAllByAccountId(UUID accountId) {
    return session()
            .createQuery("""
                    FROM Permission p
                    WHERE p.id.accountId = :accountId
                    """, Permission.class)
            .setParameter("accountId", accountId)
            .getResultList();
  }

  @Override
  public boolean existsById(Permission.PermissionId id) {
    return session()
            .createQuery("""
                    SELECT COUNT(p) FROM Permission p
                    WHERE p.id.accountId = :accountId AND p.id.surveyId = :surveyId
                    """, Long.class)
            .setParameter("accountId", id.getAccountId())
            .setParameter("surveyId", id.getSurveyId())
            .uniqueResultOptional()
            .map(count -> count > 0)
            .orElse(false);
  }

  @Override
  public void save(Permission permission) {
    session().persist(permission);
  }

  @Override
  public void update(Permission permission) {
    session().merge(permission);
  }

  @Override
  public void delete(Permission.PermissionId id) {
    Permission permission = session().find(Permission.class, id);
    if (permission != null) {
      session().remove(permission);
    }
  }
}
