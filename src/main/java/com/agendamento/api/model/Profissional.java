package com.agendamento.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

// Representa o freelancer (profissional) dono de uma agenda.
// Cada profissional tem sua própria lista de serviços, clientes,
// agendamentos e horário de expediente — totalmente isolados dos demais.
//
// É também a conta de login: o e-mail é o usuário e a senha fica
// guardada como hash BCrypt (nunca em texto puro).
@Entity
@Table(name = "profissionais")
public class Profissional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome não pode ser vazio")
    @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
    @Column(nullable = false)
    private String nome;

    // Serve como nome de usuário no login (HTTP Basic).
    @NotBlank(message = "O e-mail não pode ser vazio")
    @Email(message = "E-mail inválido")
    @Column(nullable = false, unique = true)
    private String email;

    // Hash BCrypt da senha — nunca exposto nas respostas da API.
    @Column(nullable = false)
    private String senha;

    // Área de atuação (ex: "Cabeleireiro", "Personal Trainer").
    @Column
    private String profissao;

    // Chave Pix do profissional, mostrada ao cliente na confirmação do
    // agendamento para ele efetuar o pagamento. Opcional.
    @Column
    private String chavePix;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    public void prePersist() {
        this.criadoEm = LocalDateTime.now();
    }

    public Profissional() {
    }

    // --- Getters e Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getChavePix() {
        return chavePix;
    }

    public void setChavePix(String chavePix) {
        this.chavePix = chavePix;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
}
