package com.agendamento.api.dto;

import com.agendamento.api.model.StatusAgendamento;
import jakarta.validation.constraints.NotNull;

// Dados enviados ao alterar o status de um agendamento.
public class StatusRequest {

    @NotNull(message = "O status é obrigatório (PENDENTE, CONFIRMADO, CONCLUIDO ou CANCELADO)")
    private StatusAgendamento status;

    public StatusAgendamento getStatus() {
        return status;
    }

    public void setStatus(StatusAgendamento status) {
        this.status = status;
    }
}
