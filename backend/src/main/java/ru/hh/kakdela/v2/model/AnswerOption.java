package ru.hh.kakdela.v2.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.UUID;

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
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "question_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Question question;

  @Column(name = "serial_number", nullable = false)
  private Integer serialNumber;

  @Column(name = "answer_option_text", length = 1000, nullable = false)
  private String answerOptionText;

  @Column(name = "attachment_object_key", length = 1024)
  private String attachmentObjectKey;
}
