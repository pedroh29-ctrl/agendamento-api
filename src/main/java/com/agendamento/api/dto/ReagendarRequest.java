package com.agendamento.api.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

// Dados enviados ao reagendar um compromisso para um novo horário de início.
public class ReagendarRequest {

    @NotNull(message = "O novo horário de início é obrigatório")
    @Future(message = "O novo horário de início deve estar no futuro")
    private LocalDateTime inicio;

    public LocalDateTime getInicio() {
        return inicio;
    }

    public void setInicio(LocalDateTime inicio) {
        this.inicio = inicio;
    }
}
