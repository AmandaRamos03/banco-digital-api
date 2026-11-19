package com.amanda.bancodigital.controller;

import com.amanda.bancodigital.dto.PixRequest;
import com.amanda.bancodigital.dto.PixResponse;
import com.amanda.bancodigital.service.PixService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pix")
@Tag(name = "Pix")
@SecurityRequirement(name = "bearerAuth")
public class PixController {

    private final PixService pixService;

    public PixController(PixService pixService) {
        this.pixService = pixService;
    }

    @PostMapping("/transferencias")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Faz uma transferência Pix usando CPF ou e-mail como chave")
    public PixResponse transferir(Authentication auth, @RequestBody @Valid PixRequest request) {
        return pixService.transferir(Long.valueOf(auth.getName()), request);
    }
}
