package ru.hh.kakdela.v2.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import ru.hh.kakdela.v2.model.condition.ConditionNode;

@Repository
public class ConditionNodeDaoImpl implements ConditionNodeDao {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Optional<ConditionNode> findById(UUID id) {
    return Optional.ofNullable(entityManager.find(ConditionNode.class, id));
  }

  @Override
  public void save(ConditionNode conditionNode) {
    entityManager.persist(conditionNode);
  }

  @Override
  public void update(ConditionNode conditionNode) {
    entityManager.merge(conditionNode);
  }

  @Override
  public void delete(ConditionNode conditionNode) {
    entityManager.remove(conditionNode);
  }
}
