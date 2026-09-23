package com.agendamento.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

// Representa um cliente que agenda serviços com o freelancer.
@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome não pode ser vazio")
    @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
    @Column(nullable = false)
    private String nome;

    @NotBlank(message = "O e-mail não pode ser vazio")
    @Email(message = "E-mail inválido")
    // Não é único globalmente: dois profissionais diferentes podem ter clientes
    // com o mesmo e-mail. A unicidade é garantida por profissional no service.
    @Column(nullable = false)
    private String email;

    @NotBlank(message = "O telefone não pode ser vazio")
    @Size(min = 8, max = 20, message = "O telefone deve ter entre 8 e 20 caracteres")
    @Column(nullable = false)
    private String telefone;

    // Profissional dono deste cliente. Preenchido automaticamente a partir do
    // profissional autenticado — não vem no corpo da requisição.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profissional_id", nullable = false)
    private Profissional profissional;

    // Data e hora do cadastro — preenchida automaticamente ao criar
    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    // @PrePersist: executado automaticamente pelo JPA antes de salvar no banco
    @PrePersist
    public void prePersist() {
        this.criadoEm = LocalDateTime.now();
    }

    // Construtor vazio obrigatório para o JPA
    public Cliente() {
    }

    public Cliente(String nome, String email, String telefone) {
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
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

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public Profissional getProfissional() {
        return profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
}
