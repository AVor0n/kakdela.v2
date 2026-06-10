package ru.hh.kakdela_v2.dto.answer_option;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AnswerOptionUpdateDto {

    @NotNull(message = "Порядковый номер обязателен")
    @Min(value = 1, message = "Порядковый номер должен быть больше 0")
    private Integer serialNumber;
    @NotBlank(message = "Текст варианта ответа не может быть пустым")
    private String answerOptionText;
}
