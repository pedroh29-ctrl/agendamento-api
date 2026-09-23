package com.agendamento.api.dto;

import com.agendamento.api.model.Profissional;

import java.time.LocalDateTime;

// Representação do profissional devolvida pela API — nunca inclui a senha.
public class ProfissionalResponse {

    private Long id;
    private String nome;
    private String email;
    private String profissao;
    private LocalDateTime criadoEm;

    public ProfissionalResponse(Profissional p) {
        this.id = p.getId();
        this.nome = p.getNome();
        this.email = p.getEmail();
        this.profissao = p.getProfissao();
        this.criadoEm = p.getCriadoEm();
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getProfissao() {
        return profissao;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}
