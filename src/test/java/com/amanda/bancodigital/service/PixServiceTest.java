package com.amanda.bancodigital.service;

import com.amanda.bancodigital.dto.PixRequest;
import com.amanda.bancodigital.dto.PixResponse;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PixServiceTest {

    private static final Long ID_ANA = 1L;

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private TransacaoRepository transacaoRepository;

    private PixService pixService;

    private Conta contaAna;
    private Conta contaBruno;

    @BeforeEach
    void setUp() {
        ContaService contaService = new ContaService(contaRepository, transacaoRepository);
        pixService = new PixService(contaService, contaRepository, transacaoRepository);

        contaAna = new Conta("11111111", new Cliente("Ana", "12345678909", "ana@email.com", "hash"));
        ReflectionTestUtils.setField(contaAna, "id", 10L);
        contaAna.creditar(new BigDecimal("100.00"));

        contaBruno = new Conta("22222222", new Cliente("Bruno", "98765432100", "bruno@email.com", "hash"));
        ReflectionTestUtils.setField(contaBruno, "id", 20L);

        when(contaRepository.findByClienteId(ID_ANA)).thenReturn(Optional.of(contaAna));
    }

    @Test
    void transferir_moveSaldoEntreContas() {
        when(contaRepository.findByClienteEmail("bruno@email.com")).thenReturn(Optional.of(contaBruno));

        PixResponse resposta = pixService.transferir(ID_ANA, new PixRequest("bruno@email.com", new BigDecimal("30.00")));

        assertThat(contaAna.getSaldo()).isEqualByComparingTo("70.00");
        assertThat(contaBruno.getSaldo()).isEqualByComparingTo("30.00");
        assertThat(resposta.saldoAtual()).isEqualByComparingTo("70.00");
        assertThat(resposta.destinatario()).isEqualTo("Bruno");
        assertThat(resposta.contaDestino()).isEqualTo("22222222");

        ArgumentCaptor<Transacao> captor = ArgumentCaptor.forClass(Transacao.class);
        verify(transacaoRepository, times(2)).save(captor.capture());
        List<Transacao> transacoes = captor.getAllValues();
        assertThat(transacoes).extracting(Transacao::getTipo)
                .containsExactlyInAnyOrder(TipoTransacao.PIX_ENVIADO, TipoTransacao.PIX_RECEBIDO);
        assertThat(transacoes).filteredOn(t -> t.getTipo() == TipoTransacao.PIX_ENVIADO)
                .singleElement().extracting(Transacao::getContaContraparte).isEqualTo("22222222");
        assertThat(transacoes).filteredOn(t -> t.getTipo() == TipoTransacao.PIX_RECEBIDO)
                .singleElement().extracting(Transacao::getContaContraparte).isEqualTo("11111111");
    }

    @Test
    void transferir_paraPropriaConta_lancaOperacaoInvalida() {
        when(contaRepository.findByClienteCpf("12345678909")).thenReturn(Optional.of(contaAna));

        assertThatThrownBy(() -> pixService.transferir(ID_ANA, new PixRequest("12345678909", new BigDecimal("10.00"))))
                .isInstanceOf(OperacaoInvalidaException.class)
                .hasMessage("Não é possível transferir para a própria conta");
        assertThat(contaAna.getSaldo()).isEqualByComparingTo("100.00");
    }

    @Test
    void transferir_chaveInexistente_lancaRecursoNaoEncontrado() {
        when(contaRepository.findByClienteEmail("ninguem@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pixService.transferir(ID_ANA, new PixRequest("ninguem@email.com", new BigDecimal("10.00"))))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Chave Pix não encontrada");
    }

    @Test
    void transferir_saldoInsuficiente_naoAlteraDestino() {
        when(contaRepository.findByClienteEmail("bruno@email.com")).thenReturn(Optional.of(contaBruno));

        assertThatThrownBy(() -> pixService.transferir(ID_ANA, new PixRequest("bruno@email.com", new BigDecimal("500.00"))))
                .isInstanceOf(SaldoInsuficienteException.class);
        assertThat(contaBruno.getSaldo()).isEqualByComparingTo("0.00");
        verify(transacaoRepository, never()).save(any());
    }

    @Test
    void transferir_chaveCpfComMascara_encontraDestino() {
        when(contaRepository.findByClienteCpf("98765432100")).thenReturn(Optional.of(contaBruno));

        pixService.transferir(ID_ANA, new PixRequest("987.654.321-00", new BigDecimal("10.00")));

        assertThat(contaBruno.getSaldo()).isEqualByComparingTo("10.00");
    }

    @Test
    void transferir_chaveEmailMaiuscula_encontraDestino() {
        when(contaRepository.findByClienteEmail("bruno@email.com")).thenReturn(Optional.of(contaBruno));

        pixService.transferir(ID_ANA, new PixRequest("Bruno@Email.com", new BigDecimal("10.00")));

        assertThat(contaBruno.getSaldo()).isEqualByComparingTo("10.00");
    }
}
