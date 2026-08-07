package ru.hh.kakdela.v2.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(
    name = "response_page_status",
    indexes = {
        @Index(name = "idx_response_page_status_response_id", columnList = "response_id"),
        @Index(name = "idx_response_page_status_response_id_survey_page_id",
            columnList = "response_id, survey_pages_id")})
public class ResponsePageStatus {

  @Id
  @Column(name = "id")
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "response_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Response response;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "survey_page_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private SurveyPage surveyPage;

  @Column(name = "is_included", nullable = false)
  private Boolean isIncluded;
}
