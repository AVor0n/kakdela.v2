package ru.hh.kakdela_v2.dto.subscription;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import jakarta.validation.constraints.NotNull;

@NoArgsConstructor
@Getter
@Setter
public class SubscriptionRequestDto {
    @NotNull(message = "Список почт получателей обязателен")
    private List<String> emails;
}
