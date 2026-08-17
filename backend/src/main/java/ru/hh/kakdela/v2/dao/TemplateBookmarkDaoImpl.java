package ru.hh.kakdela.v2.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.hh.kakdela.v2.model.TemplateBookmark;

@Repository
@RequiredArgsConstructor
public class TemplateBookmarkDaoImpl implements TemplateBookmarkDao {

  @PersistenceContext
  private final EntityManager entityManager;

  @Override
  public Optional<TemplateBookmark> findByAccountIdAndTemplateId(UUID accountId, UUID templateId) {
    return entityManager.createQuery(
            """
            FROM TemplateBookmark b
            WHERE b.account.id = :accountId
            AND b.template.id = :templateId
            """, TemplateBookmark.class)
        .setParameter("accountId", accountId)
        .setParameter("templateId", templateId)
        .getResultStream()
        .findFirst();
  }

  @Override
  public List<TemplateBookmark> findAllByAccountId(UUID accountId) {
    return entityManager.createQuery(
            """
            FROM TemplateBookmark b
            WHERE b.account.id = :accountId
            ORDER BY b.createdAt DESC
            """, TemplateBookmark.class)
        .setParameter("accountId", accountId)
        .getResultList();
  }

  @Override
  public boolean existsByAccountIdAndTemplateId(UUID accountId, UUID templateId) {
    Long count = entityManager.createQuery(
            """
            SELECT COUNT(b)
            FROM TemplateBookmark b
            WHERE b.account.id = :accountId
            AND b.template.id = :templateId
            """, Long.class)
        .setParameter("accountId", accountId)
        .setParameter("templateId", templateId)
        .getSingleResult();
    return count > 0;
  }

  @Override
  public void save(TemplateBookmark bookmark) {
    entityManager.persist(bookmark);
  }

  @Override
  public void delete(TemplateBookmark bookmark) {
    entityManager.remove(bookmark);
  }
}