package com.amanda.bancodigital.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transacoes")
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conta_id", nullable = false)
    private Conta conta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoTransacao tipo;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(length = 8)
    private String contaContraparte;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    protected Transacao() {
    }

    public Transacao(Conta conta, TipoTransacao tipo, BigDecimal valor, String contaContraparte) {
        this.conta = conta;
        this.tipo = tipo;
        this.valor = valor;
        this.contaContraparte = contaContraparte;
        this.dataHora = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Conta getConta() {
        return conta;
    }

    public TipoTransacao getTipo() {
        return tipo;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public String getContaContraparte() {
        return contaContraparte;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }
}
