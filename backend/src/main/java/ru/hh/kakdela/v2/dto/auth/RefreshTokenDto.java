package ru.hh.kakdela.v2.dto.auth;

import ru.hh.kakdela.v2.model.RefreshToken;

public record RefreshTokenDto(RefreshToken refreshToken, String rawRefreshToken) {}
