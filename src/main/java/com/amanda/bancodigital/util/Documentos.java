package com.amanda.bancodigital.util;

import com.amanda.bancodigital.exception.OperacaoInvalidaException;

public final class Documentos {

    private Documentos() {
    }

    public static String normalizarCpf(String cpf) {
        String apenasNumeros = cpf == null ? "" : cpf.replaceAll("\\D", "");
        if (!cpfValido(apenasNumeros)) {
            throw new OperacaoInvalidaException("CPF inválido");
        }
        return apenasNumeros;
    }

    public static String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private static boolean cpfValido(String cpf) {
        if (cpf.length() != 11 || cpf.chars().distinct().count() == 1) {
            return false;
        }
        int primeiroDigito = calcularDigito(cpf, 9);
        int segundoDigito = calcularDigito(cpf, 10);
        return primeiroDigito == Character.getNumericValue(cpf.charAt(9))
                && segundoDigito == Character.getNumericValue(cpf.charAt(10));
    }

    private static int calcularDigito(String cpf, int quantidade) {
        int soma = 0;
        int peso = quantidade + 1;
        for (int i = 0; i < quantidade; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * peso;
            peso--;
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;

    }
}
