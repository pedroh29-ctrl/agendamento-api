package com.agendamento.api.dto;

import com.agendamento.api.model.Servico;

import java.math.BigDecimal;

// Dados de um serviço expostos na página pública de agendamento.
public class ServicoPublicoResponse {

    private Long id;
    private String nome;
    private String descricao;
    private int duracaoMinutos;
    private BigDecimal preco;

    public ServicoPublicoResponse(Servico s) {
        this.id = s.getId();
        this.nome = s.getNome();
        this.descricao = s.getDescricao();
        this.duracaoMinutos = s.getDuracaoMinutos();
        this.preco = s.getPreco();
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getDuracaoMinutos() {
        return duracaoMinutos;
    }

    public BigDecimal getPreco() {
        return preco;
    }
}
