package com.amanda.bancodigital.controller;

import com.amanda.bancodigital.dto.ContaResponse;
import com.amanda.bancodigital.dto.TransacaoResponse;
import com.amanda.bancodigital.dto.ValorRequest;
import com.amanda.bancodigital.service.ContaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/contas")
@Tag(name = "Conta")
@SecurityRequirement(name = "bearerAuth")
public class ContaController {

    private final ContaService contaService;

    public ContaController(ContaService contaService) {
        this.contaService = contaService;
    }

    @GetMapping("/minha")
    @Operation(summary = "Mostra os dados e o saldo da conta")
    public ContaResponse minhaConta(Authentication auth) {
        return contaService.consultar(clienteId(auth));
    }

    @PostMapping("/deposito")
    @Operation(summary = "Deposita um valor na conta")
    public ContaResponse depositar(Authentication auth, @RequestBody @Valid ValorRequest request) {
        return contaService.depositar(clienteId(auth), request.valor());
    }

    @PostMapping("/saque")
    @Operation(summary = "Saca um valor da conta")
    public ContaResponse sacar(Authentication auth, @RequestBody @Valid ValorRequest request) {
        return contaService.sacar(clienteId(auth), request.valor());
    }

    @GetMapping("/extrato")
    @Operation(summary = "Lista as movimentações da conta, das mais recentes para as mais antigas")
    public Page<TransacaoResponse> extrato(Authentication auth,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        int tamanho = Math.clamp(size, 1, 100);
        return contaService.extrato(clienteId(auth), PageRequest.of(Math.max(page, 0), tamanho));
    }

    private Long clienteId(Authentication auth) {
        return Long.valueOf(auth.getName());
    }
}
