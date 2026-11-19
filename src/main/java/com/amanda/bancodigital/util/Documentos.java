package com.amanda.bancodigital.util;

import com.amanda.bancodigital.exception.OperacaoInvalidaException;

public final class Documentos {

    private Documentos() {
    }

    public static String normalizarCpf(String cpf) {
        String apenasNumeros = cpf == null ? "" : cpf.replaceAll("\\D", "");
        if (apenasNumeros.length() != 11) {
            throw new OperacaoInvalidaException("CPF inválido");
        }
        return apenasNumeros;
    }

    public static String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
