package com.amanda.bancodigital.service;

import com.amanda.bancodigital.dto.ContaResponse;
import com.amanda.bancodigital.dto.TransacaoResponse;
import com.amanda.bancodigital.entity.Conta;
import com.amanda.bancodigital.entity.TipoTransacao;
import com.amanda.bancodigital.entity.Transacao;
import com.amanda.bancodigital.exception.RecursoNaoEncontradoException;
import com.amanda.bancodigital.repository.ContaRepository;
import com.amanda.bancodigital.repository.TransacaoRepository;
import com.amanda.bancodigital.util.Valores;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ContaService {

    private final ContaRepository contaRepository;
    private final TransacaoRepository transacaoRepository;

    public ContaService(ContaRepository contaRepository, TransacaoRepository transacaoRepository) {
        this.contaRepository = contaRepository;
        this.transacaoRepository = transacaoRepository;
    }

    public Conta buscarPorCliente(Long clienteId) {
        return contaRepository.findByClienteId(clienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta não encontrada"));
    }

    @Transactional(readOnly = true)
    public ContaResponse consultar(Long clienteId) {
        return ContaResponse.de(buscarPorCliente(clienteId));
    }

    @Transactional
    public ContaResponse depositar(Long clienteId, BigDecimal valor) {
        BigDecimal valorValidado = Valores.validar(valor);
        Conta conta = buscarPorCliente(clienteId);

        conta.creditar(valorValidado);
        transacaoRepository.save(new Transacao(conta, TipoTransacao.DEPOSITO, valorValidado, null));
        return ContaResponse.de(conta);
    }

    @Transactional
    public ContaResponse sacar(Long clienteId, BigDecimal valor) {
        BigDecimal valorValidado = Valores.validar(valor);
        Conta conta = buscarPorCliente(clienteId);

        conta.debitar(valorValidado);
        transacaoRepository.save(new Transacao(conta, TipoTransacao.SAQUE, valorValidado, null));
        return ContaResponse.de(conta);
    }

    @Transactional(readOnly = true)
    public Page<TransacaoResponse> extrato(Long clienteId, Pageable pageable) {
        Conta conta = buscarPorCliente(clienteId);
        return transacaoRepository.findByContaIdOrderByDataHoraDesc(conta.getId(), pageable)
                .map(TransacaoResponse::de);
    }
}
