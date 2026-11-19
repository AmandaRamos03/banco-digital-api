package com.amanda.bancodigital.dto;

import com.amanda.bancodigital.entity.Conta;

import java.math.BigDecimal;

public record ContaResponse(String numero, String titular, BigDecimal saldo) {

    public static ContaResponse de(Conta conta) {
        return new ContaResponse(conta.getNumero(), conta.getCliente().getNome(), conta.getSaldo());
    }
}
