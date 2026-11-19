package com.amanda.bancodigital.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErroResponse(int status, String mensagem, LocalDateTime timestamp, Map<String, String> campos) {

    public static ErroResponse de(int status, String mensagem) {
        return new ErroResponse(status, mensagem, LocalDateTime.now(), null);
    }
}
