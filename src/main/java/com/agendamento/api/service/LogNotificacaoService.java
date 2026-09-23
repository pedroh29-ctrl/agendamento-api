package com.agendamento.api.service;

import com.agendamento.api.model.Agendamento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

// Implementação padrão de notificação: registra a mensagem no log/console.
// Não depende de servidor SMTP nem de credenciais, então funciona na hora.
//
// Para enviar e-mail de verdade, crie outra implementação de NotificacaoService
// (ex: EmailNotificacaoService com JavaMailSender), anote-a com @Primary e
// configure o SMTP em application-prod.properties. Nenhuma outra mudança é
// necessária — o AgendamentoService continua chamando a mesma interface.
@Service
public class LogNotificacaoService implements NotificacaoService {

    private static final Logger log = LoggerFactory.getLogger(LogNotificacaoService.class);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void notificarCriacao(Agendamento a) {
        enviar(a, "Seu agendamento foi criado e está PENDENTE de confirmação");
    }

    @Override
    public void notificarConfirmacao(Agendamento a) {
        enviar(a, "Seu agendamento foi CONFIRMADO");
    }

    @Override
    public void notificarCancelamento(Agendamento a) {
        enviar(a, "Seu agendamento foi CANCELADO");
    }

    @Override
    public void notificarReagendamento(Agendamento a) {
        enviar(a, "Seu agendamento foi REAGENDADO");
    }

    // Monta e "envia" (loga) a notificação. Ponto único de troca por e-mail real.
    private void enviar(Agendamento a, String assunto) {
        String destinatario = a.getCliente() != null ? a.getCliente().getEmail() : "desconhecido";
        String servico = a.getServico() != null ? a.getServico().getNome() : "serviço";
        String quando = a.getInicio() != null ? a.getInicio().format(FMT) : "-";

        log.info("[NOTIFICACAO] Para: {} | {} | Serviço: {} | Quando: {}",
                destinatario, assunto, servico, quando);
    }
}
