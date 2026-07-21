package ru.hh.kakdela.v2.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Survey survey;

  @Column(name = "title", length = 200)
  private String title;

  @Column(name = "description", columnDefinition = "text")
  private String description;

  @Column(name = "attachment_object_key", length = 1024)
  private String attachmentObjectKey;

  @Column(name = "website_url", length = 2000)
  private String websiteUrl;
}
