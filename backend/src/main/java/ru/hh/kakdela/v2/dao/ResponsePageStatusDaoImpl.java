package ru.hh.kakdela.v2.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import ru.hh.kakdela.v2.model.ResponsePageStatus;

@Repository
public class ResponsePageStatusDaoImpl implements ResponsePageStatusDao {

  @PersistenceContext
  EntityManager entityManager;

  @Override
  public Optional<ResponsePageStatus> findResponsePageStatusByResponseIdAndPageId(
      UUID responseId, UUID pageId) {
    return Optional.ofNullable(entityManager.createQuery(
            """
            SELECT rps
            FROM ResponsePageStatus rps
            WHERE rps.response.id = :responseId
            AND rps.surveyPage.id = :pageId
            """, ResponsePageStatus.class)
        .setParameter("responseId", responseId)
        .setParameter("pageId", pageId)
        .getSingleResultOrNull());
  }

  @Override
  public void resetResponsePageStatusForPagesAfterSpecifiedByResponseIdAndPageId(
      UUID responseId, UUID pageId) {
    entityManager.createQuery(
        """
        UPDATE ResponsePageStatus rps
        SET rps.isIncluded = false
        WHERE rps.response.id = :responseId
        AND rps.surveyPage.serialNumber >
        (SELECT sp.serialNumber
        FROM SurveyPage sp
        WHERE sp.id = :pageId)
        """)
        .setParameter("responseId", responseId)
        .setParameter("pageId", pageId)
        .executeUpdate();
  }

  @Override
  public void save(ResponsePageStatus responsePageStatus) {
    entityManager.persist(responsePageStatus);
  }

  @Override
  public void update(ResponsePageStatus responsePageStatus) {
    entityManager.merge(responsePageStatus);
  }

  @Override
  public void delete(ResponsePageStatus responsePageStatus) {
    entityManager.remove(responsePageStatus);
  }
}
