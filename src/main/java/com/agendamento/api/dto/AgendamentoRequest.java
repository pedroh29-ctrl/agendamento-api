package com.agendamento.api.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

// Dados enviados pelo cliente ao criar um agendamento.
// Recebe apenas os IDs de cliente e serviço (não a entidade inteira) e o
// horário de início. O horário de fim é calculado no service pela duração
// do serviço, então não é enviado aqui.
public class AgendamentoRequest {

    @NotNull(message = "O clienteId é obrigatório")
    private Long clienteId;

    @NotNull(message = "O servicoId é obrigatório")
    private Long servicoId;

    @NotNull(message = "O horário de início é obrigatório")
    @Future(message = "O horário de início deve estar no futuro")
    private LocalDateTime inicio;

    private String observacoes;

    // --- Getters e Setters ---

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getServicoId() {
        return servicoId;
    }

    public void setServicoId(Long servicoId) {
        this.servicoId = servicoId;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public void setInicio(LocalDateTime inicio) {
        this.inicio = inicio;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}
