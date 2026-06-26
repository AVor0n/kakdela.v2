package ru.hh.kakdela.v2.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

  @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Survey> authoredSurveys = new ArrayList<>();

  @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Permission> permissions = new ArrayList<>();

  @OneToMany(mappedBy = "account")
  @Builder.Default
  private List<Response> responses = new ArrayList<>();
}
