package com.amanda.bancodigital.service;

import com.amanda.bancodigital.dto.ContaResponse;
import com.amanda.bancodigital.entity.Cliente;
import com.amanda.bancodigital.entity.Conta;
import com.amanda.bancodigital.entity.TipoTransacao;
import com.amanda.bancodigital.entity.Transacao;
import com.amanda.bancodigital.exception.OperacaoInvalidaException;
import com.amanda.bancodigital.exception.RecursoNaoEncontradoException;
import com.amanda.bancodigital.exception.SaldoInsuficienteException;
import com.amanda.bancodigital.repository.ContaRepository;
import com.amanda.bancodigital.repository.TransacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContaServiceTest {

    private static final Long CLIENTE_ID = 1L;

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private TransacaoRepository transacaoRepository;

    @InjectMocks
    private ContaService contaService;

    private Conta conta;

    @BeforeEach
    void setUp() {
        conta = new Conta("12345678", new Cliente("Ana", "12345678909", "ana@email.com", "hash"));
    }

    @Test
    void depositar_aumentaSaldoERegistraTransacao() {
        when(contaRepository.findByClienteId(CLIENTE_ID)).thenReturn(Optional.of(conta));

        ContaResponse resposta = contaService.depositar(CLIENTE_ID, new BigDecimal("100.00"));

        assertThat(resposta.saldo()).isEqualByComparingTo("100.00");
        ArgumentCaptor<Transacao> captor = ArgumentCaptor.forClass(Transacao.class);
        verify(transacaoRepository).save(captor.capture());
        assertThat(captor.getValue().getTipo()).isEqualTo(TipoTransacao.DEPOSITO);
        assertThat(captor.getValue().getValor()).isEqualByComparingTo("100.00");
    }

    @Test
    void sacar_comSaldo_diminuiSaldo() {
        conta.creditar(new BigDecimal("100.00"));
        when(contaRepository.findByClienteId(CLIENTE_ID)).thenReturn(Optional.of(conta));

        ContaResponse resposta = contaService.sacar(CLIENTE_ID, new BigDecimal("40.00"));

        assertThat(resposta.saldo()).isEqualByComparingTo("60.00");
        ArgumentCaptor<Transacao> captor = ArgumentCaptor.forClass(Transacao.class);
        verify(transacaoRepository).save(captor.capture());
        assertThat(captor.getValue().getTipo()).isEqualTo(TipoTransacao.SAQUE);
    }

    @Test
    void sacar_semSaldo_lancaSaldoInsuficienteENaoRegistraTransacao() {
        when(contaRepository.findByClienteId(CLIENTE_ID)).thenReturn(Optional.of(conta));

        assertThatThrownBy(() -> contaService.sacar(CLIENTE_ID, new BigDecimal("10.00")))
                .isInstanceOf(SaldoInsuficienteException.class);
        verify(transacaoRepository, never()).save(any());
    }

    @Test
    void depositar_valorZero_lancaOperacaoInvalida() {
        assertThatThrownBy(() -> contaService.depositar(CLIENTE_ID, BigDecimal.ZERO))
                .isInstanceOf(OperacaoInvalidaException.class);
    }

    @Test
    void depositar_valorNegativo_lancaOperacaoInvalida() {
        assertThatThrownBy(() -> contaService.depositar(CLIENTE_ID, new BigDecimal("-5.00")))
                .isInstanceOf(OperacaoInvalidaException.class);
    }

    @Test
    void depositar_valorComMaisDeDuasCasasDecimais_lancaOperacaoInvalida() {
        assertThatThrownBy(() -> contaService.depositar(CLIENTE_ID, new BigDecimal("10.555")))
                .isInstanceOf(OperacaoInvalidaException.class);
    }

    @Test
    void consultar_clienteSemConta_lancaRecursoNaoEncontrado() {
        when(contaRepository.findByClienteId(CLIENTE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contaService.consultar(CLIENTE_ID))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Conta não encontrada");
    }
}
