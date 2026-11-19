package com.amanda.bancodigital.security;

import com.amanda.bancodigital.repository.ClienteRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ClienteDetailsService implements UserDetailsService {

    private final ClienteRepository clienteRepository;

    public ClienteDetailsService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        return clienteRepository.findByEmail(email)
                .map(cliente -> User.withUsername(cliente.getEmail())
                        .password(cliente.getSenha())
                        .roles("CLIENTE")
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Cliente não encontrado"));
    }
}
