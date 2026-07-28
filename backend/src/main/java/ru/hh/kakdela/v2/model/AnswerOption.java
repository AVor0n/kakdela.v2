package ru.hh.kakdela.v2.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jsoup.Jsoup;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "answer_option",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_answer_option_question_serial",
            columnNames = {"question_id", "serial_number"})
    }
)
public class AnswerOption {

  @Id
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "question_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  private Question question;

  @Column(name = "serial_number", nullable = false)
  private Integer serialNumber;

  @Column(name = "answer_option_text", length = 1000, nullable = false)
  private String answerOptionText;

  @Column(name = "attachment_object_key", length = 1024)
  private String attachmentObjectKey;

  public String getAnswerOptionTextAsPlainString() {
    return Jsoup.parseBodyFragment(answerOptionText).text();
  }
}
