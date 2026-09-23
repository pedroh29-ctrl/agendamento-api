package com.agendamento.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Dados para registrar (criar a conta de) um novo profissional.
public class RegistroProfissionalRequest {

    @NotBlank(message = "O nome não pode ser vazio")
    @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
    private String nome;

    @NotBlank(message = "O e-mail não pode ser vazio")
    @Email(message = "E-mail inválido")
    private String email;

    @NotBlank(message = "A senha não pode ser vazia")
    @Size(min = 6, max = 100, message = "A senha deve ter no mínimo 6 caracteres")
    private String senha;

    private String profissao;

    // --- Getters e Setters ---

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getProfissao() {
        return profissao;
    }

    public void setProfissao(String profissao) {
        this.profissao = profissao;
    }
}
