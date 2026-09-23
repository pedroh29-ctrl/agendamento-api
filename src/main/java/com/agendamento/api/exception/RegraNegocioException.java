package com.agendamento.api.exception;

// Exceção usada para sinalizar violações de regra de negócio,
// como conflito de horário, agendamento no passado ou e-mail duplicado.
// É tratada pelo GlobalExceptionHandler e devolvida como HTTP 409 (Conflict).
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
