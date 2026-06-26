package ru.hh.kakdela.v2.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "closing_page")
public class ClosingPage {

  @Id
  @Column(name = "survey_id", updatable = false, nullable = false)
  private UUID id;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "survey_id", nullable = false)
  private Survey survey;

  @Column(name = "title", length = 200)
  private String title;

  @Column(name = "description", length = 5000)
  private String description;

  @Column(name = "attachment_object_key", length = 1024)
  private String attachmentObjectKey;

  @Column(name = "website_url", length = 2000)
  private String websiteUrl;
}
