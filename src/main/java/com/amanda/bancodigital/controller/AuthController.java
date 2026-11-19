package com.amanda.bancodigital.controller;

import com.amanda.bancodigital.dto.CadastroRequest;
import com.amanda.bancodigital.dto.ContaResponse;
import com.amanda.bancodigital.dto.LoginRequest;
import com.amanda.bancodigital.dto.TokenResponse;
import com.amanda.bancodigital.service.AuthService;
import com.amanda.bancodigital.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação")
public class AuthController {

    private final ClienteService clienteService;
    private final AuthService authService;

    public AuthController(ClienteService clienteService, AuthService authService) {
        this.clienteService = clienteService;
        this.authService = authService;
    }

    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um cliente e abre a conta")
    public ContaResponse cadastrar(@RequestBody @Valid CadastroRequest request) {
        return clienteService.cadastrar(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Faz login e retorna o token JWT")
    public TokenResponse login(@RequestBody @Valid LoginRequest request) {
        return authService.login(request);
    }
}
