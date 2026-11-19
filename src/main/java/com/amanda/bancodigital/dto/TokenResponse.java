package com.amanda.bancodigital.dto;

import java.time.Instant;

public record TokenResponse(String token, String tipo, Instant expiraEm) {

    public TokenResponse(String token, Instant expiraEm) {
        this(token, "Bearer", expiraEm);
    }
}
