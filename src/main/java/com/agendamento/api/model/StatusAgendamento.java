package com.agendamento.api.model;

// Estados possíveis de um agendamento ao longo do seu ciclo de vida.
// PENDENTE   → recém-criado, aguardando confirmação do freelancer
// CONFIRMADO → o freelancer confirmou que vai atender
// CONCLUIDO  → o atendimento já aconteceu
// CANCELADO  → o agendamento foi cancelado (pelo cliente ou pelo freelancer)
public enum StatusAgendamento {
    PENDENTE,
    CONFIRMADO,
    CONCLUIDO,
    CANCELADO
}
