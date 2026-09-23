package com.agendamento.api.service;

import com.agendamento.api.model.Agendamento;

// Abstração de notificação ao cliente sobre o seu agendamento.
//
// A implementação padrão (LogNotificacaoService) apenas registra a mensagem
// no log — funciona sem nenhuma configuração externa. Para enviar e-mail de
// verdade, basta criar outra implementação (ex: usando JavaMailSender) e
// marcá-la como @Primary, sem tocar no restante do código.
public interface NotificacaoService {

    void notificarCriacao(Agendamento agendamento);

    void notificarConfirmacao(Agendamento agendamento);

    void notificarCancelamento(Agendamento agendamento);

    void notificarReagendamento(Agendamento agendamento);
}
