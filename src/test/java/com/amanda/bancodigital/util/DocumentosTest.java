package com.amanda.bancodigital.util;

import com.amanda.bancodigital.exception.OperacaoInvalidaException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentosTest {

    @Test
    void normalizarCpf_cpfValidoComMascara_retornaSoNumeros() {
        assertThat(Documentos.normalizarCpf("529.982.247-25")).isEqualTo("52998224725");
    }

    @Test
    void normalizarCpf_cpfValidoSemMascara_retornaOMesmo() {
        assertThat(Documentos.normalizarCpf("11144477735")).isEqualTo("11144477735");
    }

    @ParameterizedTest
    @ValueSource(strings = {"529.982.247-24", "111.444.777-36", "12345678900"})
    void normalizarCpf_digitoVerificadorErrado_lancaExcecao(String cpf) {
        assertThatThrownBy(() -> Documentos.normalizarCpf(cpf))
                .isInstanceOf(OperacaoInvalidaException.class)
                .hasMessage("CPF inválido");
    }

    @ParameterizedTest
    @ValueSource(strings = {"000.000.000-00", "111.111.111-11", "99999999999"})
    void normalizarCpf_todosOsDigitosIguais_lancaExcecao(String cpf) {
        assertThatThrownBy(() -> Documentos.normalizarCpf(cpf))
                .isInstanceOf(OperacaoInvalidaException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"123", "1234567890123", "", "abc"})
    void normalizarCpf_quantidadeErradaDeDigitos_lancaExcecao(String cpf) {
        assertThatThrownBy(() -> Documentos.normalizarCpf(cpf))
                .isInstanceOf(OperacaoInvalidaException.class);
    }
}