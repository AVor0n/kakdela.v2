package ru.hh.kakdela.v2.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "account")
public class Account {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "login", length = 32, nullable = false, unique = true)
  private String login;

  @Column(name = "email", length = 254, nullable = false, unique = true)
  private String email;

  @Column(name = "password_hash", columnDefinition = "text", nullable = false)
  private String passwordHash;

  @Column(name = "registered_at", updatable = false, nullable = false)
  private Instant registeredAt;

  @Column(name = "token_version", nullable = false)
  private Integer tokenVersion;

  @Column(name = "is_deleted", nullable = false)
  private Boolean isDeleted;


  @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Survey> authoredSurveys = new ArrayList<>();

  @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Permission> permissions = new ArrayList<>();

  @OneToMany(mappedBy = "account")
  @Builder.Default
  private List<Response> responses = new ArrayList<>();

  @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<RefreshToken> refreshTokens = new ArrayList<>();
}
