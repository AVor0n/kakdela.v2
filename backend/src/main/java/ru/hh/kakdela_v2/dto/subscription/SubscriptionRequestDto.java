package ru.hh.kakdela_v2.dto.subscription;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class SubscriptionRequestDto {

    private List<UUID> userIds;
}
