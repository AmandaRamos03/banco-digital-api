package com.amanda.bancodigital.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PixResponse(BigDecimal valor, String destinatario, String contaDestino,
                          BigDecimal saldoAtual, LocalDateTime dataHora) {
}
