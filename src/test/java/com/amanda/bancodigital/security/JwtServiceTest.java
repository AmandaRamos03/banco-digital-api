package com.amanda.bancodigital.security;

import com.amanda.bancodigital.entity.Cliente;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SEGREDO = "um-segredo-de-teste-com-mais-de-32-caracteres";

    private Cliente clienteComId(Long id) {
        Cliente cliente = new Cliente("Ana", "12345678909", "ana@email.com", "hash");
        ReflectionTestUtils.setField(cliente, "id", id);
        return cliente;
    }

    @Test
    void gerarToken_eExtrairId_retornaMesmoId() {
        JwtService jwtService = new JwtService(SEGREDO, 120);

        String token = jwtService.gerarToken(clienteComId(42L));

        assertThat(jwtService.extrairClienteId(token)).isEqualTo(42L);
    }

    @Test
    void tokenAdulterado_lancaJwtException() {
        JwtService jwtService = new JwtService(SEGREDO, 120);
        String token = jwtService.gerarToken(clienteComId(42L));
        String adulterado = token.substring(0, token.length() - 3) + "abc";

        assertThatThrownBy(() -> jwtService.extrairClienteId(adulterado))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void tokenAssinadoComOutroSegredo_lancaJwtException() {
        JwtService outro = new JwtService("outro-segredo-diferente-com-mais-de-32-caracteres", 120);
        String token = outro.gerarToken(clienteComId(42L));

        assertThatThrownBy(() -> new JwtService(SEGREDO, 120).extrairClienteId(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void tokenExpirado_lancaJwtException() {
        JwtService jwtService = new JwtService(SEGREDO, -1);
        String token = jwtService.gerarToken(clienteComId(42L));

        assertThatThrownBy(() -> jwtService.extrairClienteId(token))
                .isInstanceOf(JwtException.class);
    }
}
