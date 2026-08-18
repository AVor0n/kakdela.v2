package ru.hh.kakdela.v2.dto.account;

import ru.hh.kakdela.v2.dto.auth.AuthTokensDto;

public record HhLinkConfirmResultDto(AccountResponseDto account, AuthTokensDto tokens) {
}
