package com.agendamento.api.exception;

// Exceção usada quando um recurso referenciado não existe
// (ex: agendar com um clienteId ou servicoId inexistente).
// É tratada pelo GlobalExceptionHandler e devolvida como HTTP 404 (Not Found).
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
