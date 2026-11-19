package com.amanda.bancodigital.service;

import com.amanda.bancodigital.dto.CadastroRequest;
import com.amanda.bancodigital.dto.ContaResponse;
import com.amanda.bancodigital.entity.Cliente;
import com.amanda.bancodigital.entity.Conta;
import com.amanda.bancodigital.exception.OperacaoInvalidaException;
import com.amanda.bancodigital.exception.RecursoDuplicadoException;
import com.amanda.bancodigital.repository.ClienteRepository;
import com.amanda.bancodigital.repository.ContaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ContaRepository contaRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private ClienteService clienteService;

    @BeforeEach
    void setUp() {
        clienteService = new ClienteService(clienteRepository, contaRepository, passwordEncoder);
    }

    private void repositoriosSalvamOQueRecebem() {
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));
        when(contaRepository.save(any(Conta.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void cadastrar_criaClienteEContaComSaldoZero() {
        repositoriosSalvamOQueRecebem();
        CadastroRequest request = new CadastroRequest("Ana Souza", "12345678909", "ana@email.com", "senha123");

        ContaResponse resposta = clienteService.cadastrar(request);

        assertThat(resposta.saldo()).isEqualByComparingTo("0.00");
        assertThat(resposta.numero()).matches("\\d{8}");
        assertThat(resposta.titular()).isEqualTo("Ana Souza");

        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(captor.capture());
        assertThat(captor.getValue().getSenha()).isNotEqualTo("senha123");
        assertThat(passwordEncoder.matches("senha123", captor.getValue().getSenha())).isTrue();
    }

    @Test
    void cadastrar_cpfJaExiste_lancaRecursoDuplicado() {
        when(clienteRepository.existsByCpf("12345678909")).thenReturn(true);
        CadastroRequest request = new CadastroRequest("Ana", "12345678909", "ana@email.com", "senha123");

        assertThatThrownBy(() -> clienteService.cadastrar(request))
                .isInstanceOf(RecursoDuplicadoException.class)
                .hasMessage("CPF já cadastrado");
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void cadastrar_emailJaExiste_lancaRecursoDuplicado() {
        when(clienteRepository.existsByEmail("ana@email.com")).thenReturn(true);
        CadastroRequest request = new CadastroRequest("Ana", "12345678909", "ana@email.com", "senha123");

        assertThatThrownBy(() -> clienteService.cadastrar(request))
                .isInstanceOf(RecursoDuplicadoException.class)
                .hasMessage("E-mail já cadastrado");
    }

    @Test
    void cadastrar_normalizaCpfComMascaraEEmailMaiusculo() {
        repositoriosSalvamOQueRecebem();
        CadastroRequest request = new CadastroRequest("Ana", "123.456.789-09", " Ana@Email.COM ", "senha123");

        clienteService.cadastrar(request);

        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(captor.capture());
        assertThat(captor.getValue().getCpf()).isEqualTo("12345678909");
        assertThat(captor.getValue().getEmail()).isEqualTo("ana@email.com");
    }

    @Test
    void cadastrar_cpfComQuantidadeErradaDeDigitos_lancaOperacaoInvalida() {
        CadastroRequest request = new CadastroRequest("Ana", "1234", "ana@email.com", "senha123");

        assertThatThrownBy(() -> clienteService.cadastrar(request))
                .isInstanceOf(OperacaoInvalidaException.class)
                .hasMessage("CPF inválido");
    }
}
