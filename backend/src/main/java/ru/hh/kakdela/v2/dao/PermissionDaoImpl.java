package ru.hh.kakdela.v2.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import lombok.extern.slf4j.Slf4j;
import ru.hh.kakdela.v2.model.Permission;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
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
  public List<UUID> findUserIdsBySurveyIdAndRole(UUID surveyId, Permission.SurveyRole role) {
    return entityManager
        .createQuery(
            """
            SELECT p.id.accountId
            FROM Permission p
            WHERE p.id.surveyId = :surveyId AND p.role = :role
            """,
            UUID.class
        )
        .setParameter("surveyId", surveyId)
        .setParameter("role", role)
        .getResultList();
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
    log.debug("Сохранены права доступа id={}", permission.getId());
    entityManager.persist(permission);
  }

  @Override
  public void update(Permission permission) {
    log.debug("Изменены права доступа id={}", permission.getId());
    entityManager.merge(permission);
  }

  @Override
  public void delete(Permission permission) {
    log.debug("Удалены права доступа id={}", permission.getId());
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
