package com.amanda.bancodigital.service;

import com.amanda.bancodigital.dto.CadastroRequest;
import com.amanda.bancodigital.dto.ContaResponse;
import com.amanda.bancodigital.entity.Cliente;
import com.amanda.bancodigital.entity.Conta;
import com.amanda.bancodigital.exception.RecursoDuplicadoException;
import com.amanda.bancodigital.repository.ClienteRepository;
import com.amanda.bancodigital.repository.ContaRepository;
import com.amanda.bancodigital.util.Documentos;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ContaRepository contaRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom random = new SecureRandom();

    public ClienteService(ClienteRepository clienteRepository,
                          ContaRepository contaRepository,
                          PasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.contaRepository = contaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public ContaResponse cadastrar(CadastroRequest request) {
        String cpf = Documentos.normalizarCpf(request.cpf());
        String email = Documentos.normalizarEmail(request.email());

        if (clienteRepository.existsByCpf(cpf)) {
            throw new RecursoDuplicadoException("CPF já cadastrado");
        }
        if (clienteRepository.existsByEmail(email)) {
            throw new RecursoDuplicadoException("E-mail já cadastrado");
        }

        Cliente cliente = new Cliente(request.nome().trim(), cpf, email, passwordEncoder.encode(request.senha()));
        clienteRepository.save(cliente);

        Conta conta = contaRepository.save(new Conta(gerarNumeroConta(), cliente));
        return ContaResponse.de(conta);
    }

    private String gerarNumeroConta() {
        String numero;
        do {
            numero = String.format("%08d", random.nextInt(100_000_000));
        } while (contaRepository.existsByNumero(numero));
        return numero;
    }
}
