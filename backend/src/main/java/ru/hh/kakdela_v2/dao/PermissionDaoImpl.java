package ru.hh.kakdela_v2.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import ru.hh.kakdela_v2.model.Permission;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PermissionDaoImpl implements PermissionDao {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Optional<Permission> findById(Permission.PermissionId id) {
    return Optional.ofNullable(entityManager.find(Permission.class, id));
  }

  @Override
  public Optional<Permission> findBySurveyIdAndAccountId(UUID surveyId, UUID accountId) {
    try {
      return Optional.of(entityManager
              .createQuery("""
                      FROM Permission p
                      WHERE p.id.surveyId = :surveyId AND p.id.accountId = :accountId
                      """, Permission.class)
              .setParameter("surveyId", surveyId)
              .setParameter("accountId", accountId)
              .getSingleResultOrNull());
    } catch (NoResultException e) {
      return Optional.empty();
    }
  }

  @Override
  public List<Permission> findAllBySurveyId(UUID surveyId) {
    return entityManager
            .createQuery("FROM Permission p WHERE p.id.surveyId = :surveyId", Permission.class)
            .setParameter("surveyId", surveyId)
            .getResultList();
  }

  @Override
  public List<Permission> findAllByAccountId(UUID accountId) {
    return entityManager
            .createQuery("FROM Permission p WHERE p.id.accountId = :accountId", Permission.class)
            .setParameter("accountId", accountId)
            .getResultList();
  }

  @Override
  public boolean existsById(Permission.PermissionId id) {
    return Optional.of(entityManager
                    .createQuery("""
                            SELECT COUNT(p) FROM Permission p
                            WHERE p.id.accountId = :accountId AND p.id.surveyId = :surveyId
                            """, Long.class)
                    .setParameter("accountId", id.getAccountId())
                    .setParameter("surveyId", id.getSurveyId())
                    .getSingleResultOrNull())
            .map(count -> count > 0)
            .orElse(false);
  }

  @Override
  public boolean existsBySurveyIdAndAccountId(UUID surveyId, UUID accountId) {
    return Optional.of(entityManager
                    .createQuery("""
                            SELECT COUNT(p) FROM Permission p
                            WHERE p.id.surveyId = :surveyId AND p.id.accountId = :accountId
                            """, Long.class)
                    .setParameter("surveyId", surveyId)
                    .setParameter("accountId", accountId)
                    .getSingleResultOrNull())
            .map(count -> count > 0)
            .orElse(false);
  }

  @Override
  public void save(Permission permission) {
    entityManager.persist(permission);
  }

  @Override
  public void update(Permission permission) {
    entityManager.merge(permission);
  }

  @Override
  public void delete(Permission permission) {
    entityManager.remove(permission);
  }

  @Override
  public void deleteBySurveyIdAndAccountId(UUID surveyId, UUID accountId) {
    entityManager
            .createQuery("""
                    DELETE FROM Permission p
                    WHERE p.id.surveyId = :surveyId AND p.id.accountId = :accountId
                    """)
            .setParameter("surveyId", surveyId)
            .setParameter("accountId", accountId)
            .executeUpdate();
  }
}
