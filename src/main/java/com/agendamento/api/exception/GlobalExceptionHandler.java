package com.agendamento.api.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

// @RestControllerAdvice: intercepta exceções lançadas em qualquer controller
// e retorna respostas JSON amigáveis em vez de stack traces.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Captura erros de @Valid em @RequestBody (ex: nome vazio, início no passado)
    // Retorna 400 com um mapa campo → mensagem de erro
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    // Captura erros de validação em @RequestParam / @PathVariable
    // Retorna 400 com mensagem de erro
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolation(
            ConstraintViolationException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getConstraintViolations().forEach(violation -> {
            String field = violation.getPropertyPath().toString();
            errors.put(field, violation.getMessage());
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    // Recurso referenciado não existe (ex: agendar com clienteId inválido)
    // Retorna 404 Not Found
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> handleRecursoNaoEncontrado(
            RecursoNaoEncontradoException ex) {

        Map<String, String> body = new HashMap<>();
        body.put("erro", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // Violação de regra de negócio (conflito de horário, e-mail duplicado,
    // transição de status inválida). Retorna 409 Conflict.
    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<Map<String, String>> handleRegraNegocio(
            RegraNegocioException ex) {

        Map<String, String> body = new HashMap<>();
        body.put("erro", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }
}
