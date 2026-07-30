package ru.hh.kakdela.v2.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import ru.hh.kakdela.v2.model.condition.Condition;

public interface ConditionDao {

  Optional<Condition> findById(UUID id);

  List<Condition> findAllByPageId(UUID pageId);

  void save(Condition condition);

  void update(Condition condition);

  void delete(Condition condition);
}
