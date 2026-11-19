package com.amanda.bancodigital.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void saldoInsuficiente_retorna422ComMensagem() {
        ResponseEntity<ErroResponse> resposta = handler.tratarRegraNegocio(new SaldoInsuficienteException());

        assertThat(resposta.getStatusCode().value()).isEqualTo(422);
        assertThat(resposta.getBody().mensagem()).isEqualTo("Saldo insuficiente");
    }

    @Test
    void recursoDuplicado_retorna409() {
        ResponseEntity<ErroResponse> resposta = handler.tratarRegraNegocio(new RecursoDuplicadoException("CPF já cadastrado"));

        assertThat(resposta.getStatusCode().value()).isEqualTo(409);
        assertThat(resposta.getBody().mensagem()).isEqualTo("CPF já cadastrado");
    }

    @Test
    void recursoNaoEncontrado_retorna404() {
        ResponseEntity<ErroResponse> resposta = handler.tratarRegraNegocio(new RecursoNaoEncontradoException("Conta não encontrada"));

        assertThat(resposta.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void rotaInexistente_retorna404() {
        var ex = new NoResourceFoundException(HttpMethod.GET, "/naoexiste", "naoexiste");

        ResponseEntity<ErroResponse> resposta = handler.tratarRotaInexistente(ex);

        assertThat(resposta.getStatusCode().value()).isEqualTo(404);
        assertThat(resposta.getBody().mensagem()).isEqualTo("Rota não encontrada");
    }

    @Test
    void metodoNaoPermitido_retorna405() {
        ResponseEntity<ErroResponse> resposta = handler.tratarMetodoNaoPermitido(new HttpRequestMethodNotSupportedException("GET"));

        assertThat(resposta.getStatusCode().value()).isEqualTo(405);
        assertThat(resposta.getBody().mensagem()).isEqualTo("Método não permitido");
    }

    @Test
    void contentTypeNaoSuportado_retorna415() {
        var ex = new HttpMediaTypeNotSupportedException("Content-Type 'text/plain' is not supported");

        ResponseEntity<ErroResponse> resposta = handler.tratarContentTypeInvalido(ex);

        assertThat(resposta.getStatusCode().value()).isEqualTo(415);
        assertThat(resposta.getBody().mensagem()).isEqualTo("Content-Type deve ser application/json");
    }

    @Test
    void parametroComTipoErrado_retorna400ComNomeDoParametro() {
        var ex = new MethodArgumentTypeMismatchException("abc", Integer.class, "page", null, null);

        ResponseEntity<ErroResponse> resposta = handler.tratarParametroInvalido(ex);

        assertThat(resposta.getStatusCode().value()).isEqualTo(400);
        assertThat(resposta.getBody().mensagem()).isEqualTo("Parâmetro inválido: page");
    }

    @Test
    void erroInesperado_retorna500SemDetalhesInternos() {
        ResponseEntity<ErroResponse> resposta = handler.tratarErroInesperado(new IllegalStateException("detalhe interno"));

        assertThat(resposta.getStatusCode().value()).isEqualTo(500);
        assertThat(resposta.getBody().mensagem()).isEqualTo("Erro interno");
    }
}
