package com.amanda.bancodigital.util;

import com.amanda.bancodigital.exception.OperacaoInvalidaException;

import java.math.BigDecimal;

public final class Valores {

    private Valores() {
    }

    public static BigDecimal validar(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            throw new OperacaoInvalidaException("O valor deve ser maior que zero");
        }
        if (valor.stripTrailingZeros().scale() > 2) {
            throw new OperacaoInvalidaException("O valor deve ter no máximo 2 casas decimais");
        }
        return valor.setScale(2);
    }
}
