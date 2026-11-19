package com.amanda.bancodigital.exception;

public class OperacaoInvalidaException extends RegraNegocioException {

    public OperacaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}
