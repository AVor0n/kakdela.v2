package ru.hh.kakdela.v2.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.hh.kakdela.v2.model.condition.Condition;

@Repository
public class ConditionDaoImpl implements ConditionDao {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Optional<Condition> findById(UUID id) {
    return Optional.ofNullable(entityManager.find(Condition.class, id));
  }

  @Override
  public List<Condition> findAllByPageId(UUID pageId) {
    return entityManager.createQuery(
         """
         FROM Condition c
         WHERE c.surveyPage.id = :pageId
         """, Condition.class)
        .setParameter("pageId", pageId)
        .getResultList();
  }

  @Override
  public void save(Condition condition) {
    entityManager.persist(condition);
  }

  @Override
  public void update(Condition condition) {
    entityManager.merge(condition);
  }

  @Override
  public void delete(Condition condition) {
    entityManager.remove(condition);
  }
}
