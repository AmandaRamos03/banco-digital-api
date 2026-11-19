package com.amanda.bancodigital.repository;

import com.amanda.bancodigital.entity.Conta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContaRepository extends JpaRepository<Conta, Long> {

    Optional<Conta> findByClienteId(Long clienteId);

    Optional<Conta> findByClienteCpf(String cpf);

    Optional<Conta> findByClienteEmail(String email);

    boolean existsByNumero(String numero);
}
