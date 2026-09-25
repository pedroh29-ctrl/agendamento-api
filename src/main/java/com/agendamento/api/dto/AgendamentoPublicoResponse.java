package com.agendamento.api.dto;

import com.agendamento.api.model.Agendamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Confirmação devolvida ao cliente após marcar um horário na página pública.
// Mostra só o essencial, incluindo o valor e a chave Pix para pagamento.
public class AgendamentoPublicoResponse {

    private Long id;
    private String profissional;
    private String servico;
    private String cliente;
    private LocalDateTime inicio;
    private LocalDateTime fim;
    private String status;
    private BigDecimal preco;
    private String chavePix;

    public AgendamentoPublicoResponse(Agendamento a) {
        this.id = a.getId();
        this.profissional = a.getProfissional() != null ? a.getProfissional().getNome() : null;
        this.servico = a.getServico() != null ? a.getServico().getNome() : null;
        this.cliente = a.getCliente() != null ? a.getCliente().getNome() : null;
        this.inicio = a.getInicio();
        this.fim = a.getFim();
        this.status = a.getStatus() != null ? a.getStatus().name() : null;
        this.preco = a.getServico() != null ? a.getServico().getPreco() : null;
        this.chavePix = a.getProfissional() != null ? a.getProfissional().getChavePix() : null;
    }

    public Long getId() {
        return id;
    }

    public String getProfissional() {
        return profissional;
    }

    public String getServico() {
        return servico;
    }

    public String getCliente() {
        return cliente;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public LocalDateTime getFim() {
        return fim;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public String getChavePix() {
        return chavePix;
    }
}
