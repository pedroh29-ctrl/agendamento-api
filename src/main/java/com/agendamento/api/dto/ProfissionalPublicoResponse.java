package com.agendamento.api.dto;

import com.agendamento.api.model.Profissional;

// Dados mínimos de um profissional expostos na página pública de agendamento.
// Nunca inclui e-mail de login, senha ou dados sensíveis.
public class ProfissionalPublicoResponse {

    private Long id;
    private String nome;
    private String profissao;

    public ProfissionalPublicoResponse(Profissional p) {
        this.id = p.getId();
        this.nome = p.getNome();
        this.profissao = p.getProfissao();
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getProfissao() {
        return profissao;
    }
}
