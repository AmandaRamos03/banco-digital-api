package com.amanda.bancodigital.service;

import com.amanda.bancodigital.dto.PixRequest;
import com.amanda.bancodigital.dto.PixResponse;
import com.amanda.bancodigital.entity.Conta;
import com.amanda.bancodigital.entity.TipoTransacao;
import com.amanda.bancodigital.entity.Transacao;
import com.amanda.bancodigital.exception.OperacaoInvalidaException;
import com.amanda.bancodigital.exception.RecursoNaoEncontradoException;
import com.amanda.bancodigital.repository.ContaRepository;
import com.amanda.bancodigital.repository.TransacaoRepository;
import com.amanda.bancodigital.util.Documentos;
import com.amanda.bancodigital.util.Valores;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PixService {

    private final ContaService contaService;
    private final ContaRepository contaRepository;
    private final TransacaoRepository transacaoRepository;

    public PixService(ContaService contaService,
                      ContaRepository contaRepository,
                      TransacaoRepository transacaoRepository) {
        this.contaService = contaService;
        this.contaRepository = contaRepository;
        this.transacaoRepository = transacaoRepository;
    }

    @Transactional
    public PixResponse transferir(Long clienteId, PixRequest request) {
        BigDecimal valor = Valores.validar(request.valor());
        Conta origem = contaService.buscarPorCliente(clienteId);
        Conta destino = buscarPorChave(request.chave());

        if (origem.getId().equals(destino.getId())) {
            throw new OperacaoInvalidaException("Não é possível transferir para a própria conta");
        }

        origem.debitar(valor);
        destino.creditar(valor);

        transacaoRepository.save(new Transacao(origem, TipoTransacao.PIX_ENVIADO, valor, destino.getNumero()));
        transacaoRepository.save(new Transacao(destino, TipoTransacao.PIX_RECEBIDO, valor, origem.getNumero()));

        return new PixResponse(valor, destino.getCliente().getNome(), destino.getNumero(),
                origem.getSaldo(), LocalDateTime.now());
    }

    private Conta buscarPorChave(String chave) {
        Optional<Conta> conta = chave.contains("@")
                ? contaRepository.findByClienteEmail(Documentos.normalizarEmail(chave))
                : contaRepository.findByClienteCpf(normalizarCpfOuNada(chave));

        return conta.orElseThrow(() -> new RecursoNaoEncontradoException("Chave Pix não encontrada"));
    }

    private String normalizarCpfOuNada(String chave) {
        try {
            return Documentos.normalizarCpf(chave);
        } catch (OperacaoInvalidaException e) {
            throw new RecursoNaoEncontradoException("Chave Pix não encontrada");
        }
    }
}
