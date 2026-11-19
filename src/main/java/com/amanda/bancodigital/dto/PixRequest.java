package com.amanda.bancodigital.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PixRequest(
        @NotBlank(message = "Chave Pix é obrigatória")
        String chave,

        @NotNull(message = "Valor é obrigatório")
        @Positive(message = "O valor deve ser maior que zero")
        @Digits(integer = 13, fraction = 2, message = "O valor deve ter no máximo 2 casas decimais")
        BigDecimal valor
) {
}
