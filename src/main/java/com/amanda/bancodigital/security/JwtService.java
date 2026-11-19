package com.amanda.bancodigital.security;

import com.amanda.bancodigital.entity.Cliente;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey chave;
    private final Duration expiracao;

    public JwtService(@Value("${app.jwt.secret}") String segredo,
                      @Value("${app.jwt.expiracao-minutos}") long expiracaoMinutos) {
        this.chave = Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
        this.expiracao = Duration.ofMinutes(expiracaoMinutos);
    }

    public String gerarToken(Cliente cliente) {
        Instant agora = Instant.now();
        return Jwts.builder()
                .subject(cliente.getId().toString())
                .claim("email", cliente.getEmail())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plus(expiracao)))
                .signWith(chave)
                .compact();
    }

    public Long extrairClienteId(String token) {
        String subject = Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
        return Long.valueOf(subject);
    }

    public Instant calcularExpiracao() {
        return Instant.now().plus(expiracao);
    }
}
