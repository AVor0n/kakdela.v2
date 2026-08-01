package ru.hh.kakdela.v2.dao;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import ru.hh.kakdela.v2.model.RefreshToken;

public interface RefreshTokenDao {

  Optional<RefreshToken> findByTokenHash(String tokenHash);

  List<RefreshToken> findActiveByAccountId(UUID accountId, Instant now);

  void save(RefreshToken refreshToken);

  void update(RefreshToken refreshToken);

  void delete(RefreshToken refreshToken);

  void deleteByTokenHash(String tokenHash);

  void deleteAllByAccountId(UUID accountId);

  void deleteAllByAccountIdAndDeviceId(UUID accountId, String deviceId);

  void deleteAllExpired(Instant now);
}
