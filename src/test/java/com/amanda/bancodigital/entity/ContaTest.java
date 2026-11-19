package com.amanda.bancodigital.entity;

import com.amanda.bancodigital.exception.SaldoInsuficienteException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContaTest {

    @Test
    void creditar_somaValorAoSaldo() {
        Conta conta = new Conta("12345678", new Cliente());

        conta.creditar(new BigDecimal("50.00"));

        assertThat(conta.getSaldo()).isEqualByComparingTo("50.00");
    }

    @Test
    void debitar_semSaldoSuficiente_lancaExcecaoEMantemSaldo() {
        Conta conta = new Conta("12345678", new Cliente());
        conta.creditar(new BigDecimal("20.00"));

        assertThatThrownBy(() -> conta.debitar(new BigDecimal("30.00")))
                .isInstanceOf(SaldoInsuficienteException.class);

        assertThat(conta.getSaldo()).isEqualByComparingTo("20.00");
    }

    @Test
    void debitar_comSaldo_subtraiValor() {
        Conta conta = new Conta("12345678", new Cliente());
        conta.creditar(new BigDecimal("100.00"));

        conta.debitar(new BigDecimal("40.00"));

        assertThat(conta.getSaldo()).isEqualByComparingTo("60.00");
    }
}
