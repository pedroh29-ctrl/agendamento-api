package com.agendamento.api.dto;

import jakarta.validation.constraints.Size;

// Dados que o profissional pode atualizar no próprio perfil.
// Não permite trocar e-mail (login) nem senha por aqui — só os dados de
// exibição e a chave Pix usada para receber os pagamentos.
public class AtualizarPerfilRequest {

    @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
    private String nome;

    private String profissao;

    @Size(max = 140, message = "A chave Pix deve ter no máximo 140 caracteres")
    private String chavePix;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getProfissao() {
        return profissao;
    }

    public void setProfissao(String profissao) {
        this.profissao = profissao;
    }

    public String getChavePix() {
        return chavePix;
    }

    public void setChavePix(String chavePix) {
        this.chavePix = chavePix;
    }
}
