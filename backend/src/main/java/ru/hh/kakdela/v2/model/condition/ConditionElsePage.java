package ru.hh.kakdela.v2.model.condition;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.hh.kakdela.v2.model.SurveyPage;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "condition_else_page")
public class ConditionElsePage {

  @Id
  @MapsId
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "survey_page_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private SurveyPage surveyPage;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "else_page_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private SurveyPage elsePage;

}
