package com.amanda.bancodigital.service;

import com.amanda.bancodigital.dto.LoginRequest;
import com.amanda.bancodigital.dto.TokenResponse;
import com.amanda.bancodigital.entity.Cliente;
import com.amanda.bancodigital.repository.ClienteRepository;
import com.amanda.bancodigital.security.JwtService;
import com.amanda.bancodigital.util.Documentos;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final ClienteRepository clienteRepository;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager,
                       ClienteRepository clienteRepository,
                       JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.clienteRepository = clienteRepository;
        this.jwtService = jwtService;
    }

    public TokenResponse login(LoginRequest request) {
        String email = Documentos.normalizarEmail(request.email());
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.senha()));

        Cliente cliente = clienteRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("E-mail ou senha inválidos"));

        return new TokenResponse(jwtService.gerarToken(cliente), jwtService.calcularExpiracao());
    }
}
