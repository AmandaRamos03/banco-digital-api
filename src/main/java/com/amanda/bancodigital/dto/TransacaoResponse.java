package com.amanda.bancodigital.dto;

import com.amanda.bancodigital.entity.TipoTransacao;
import com.amanda.bancodigital.entity.Transacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransacaoResponse(TipoTransacao tipo, BigDecimal valor, String contaContraparte, LocalDateTime dataHora) {

    public static TransacaoResponse de(Transacao transacao) {
        return new TransacaoResponse(transacao.getTipo(), transacao.getValor(),
                transacao.getContaContraparte(), transacao.getDataHora());
    }
}
