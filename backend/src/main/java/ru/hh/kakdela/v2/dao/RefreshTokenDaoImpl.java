package ru.hh.kakdela.v2.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.hh.kakdela.v2.model.RefreshToken;

@Slf4j
@Repository
public class RefreshTokenDaoImpl implements RefreshTokenDao {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Optional<RefreshToken> findByTokenHash(String tokenHash) {
    return Optional.ofNullable(
        entityManager
            .createQuery(
                """
                FROM RefreshToken rt
                WHERE rt.tokenHash = :tokenHash
                """, RefreshToken.class)
            .setParameter("tokenHash", tokenHash)
            .getSingleResultOrNull()
    );
  }

  @Override
  public List<RefreshToken> findActiveByAccountId(UUID accountId, Instant now) {
    return entityManager
        .createQuery(
            """
            FROM RefreshToken rt
            WHERE rt.account.id = :accountId AND rt.expiresAt > :now
            ORDER BY rt.createdAt DESC
            """, RefreshToken.class)
        .setParameter("accountId", accountId)
        .setParameter("now", now)
        .getResultList();
  }

  @Override
  public void save(RefreshToken refreshToken) {
    log.debug("Сохраняем refresh токен: accountId={}, deviceId={}",
        refreshToken.getAccount().getId(), refreshToken.getDeviceId());
    entityManager.persist(refreshToken);
  }

  @Override
  public void update(RefreshToken refreshToken) {
    log.debug("Обновляем refresh токен: id={}", refreshToken.getId());
    entityManager.merge(refreshToken);
  }

  @Override
  public void delete(RefreshToken refreshToken) {
    log.debug("Удаляем refresh токен: id={}", refreshToken.getId());
    entityManager.remove(refreshToken);
  }

  @Override
  public void deleteByTokenHash(String tokenHash) {
    log.debug("Удаляем refresh токен по hash");
    entityManager
        .createQuery(
            """
            DELETE FROM RefreshToken rt 
            WHERE rt.tokenHash = :tokenHash
            """)
        .setParameter("tokenHash", tokenHash)
        .executeUpdate();
  }

  @Override
  public void deleteAllByAccountId(UUID accountId) {
    log.debug("Удаляем все refresh токены для accountId={}", accountId);
    entityManager
        .createQuery(
            """
            DELETE FROM RefreshToken rt
            WHERE rt.account.id = :accountId
            """)
        .setParameter("accountId", accountId)
        .executeUpdate();
  }

  @Override
  public void deleteAllByAccountIdAndDeviceId(UUID accountId, String deviceId) {
    log.debug("Удаляем refresh токены для accountId={}, deviceId={}", accountId, deviceId);
    entityManager
        .createQuery(
            """
            DELETE FROM RefreshToken rt
            WHERE rt.account.id = :accountId AND rt.deviceId = :deviceId
            """)
        .setParameter("accountId", accountId)
        .setParameter("deviceId", deviceId)
        .executeUpdate();
  }

  @Override
  public void deleteAllExpired(Instant now) {
    log.debug("Удаляем истекшие refresh токены (до {})", now);
    entityManager
        .createQuery(
            """
            DELETE FROM RefreshToken rt 
            WHERE rt.expiresAt < :now
            """)
        .setParameter("now", now)
        .executeUpdate();
  }
}
