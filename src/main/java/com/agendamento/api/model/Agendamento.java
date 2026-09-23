package com.agendamento.api.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

// Representa um compromisso na agenda do freelancer: um cliente reservou
// um serviço para um horário específico.
//
// O horário de início é informado ao criar o agendamento; o horário de fim
// é calculado automaticamente somando a duração do serviço.
@Entity
@Table(name = "agendamentos")
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Profissional dono deste agendamento. Preenchido a partir do profissional
    // autenticado — a agenda de cada profissional é isolada.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profissional_id", nullable = false)
    private Profissional profissional;

    // Muitos agendamentos podem pertencer a um mesmo cliente.
    // FetchType.EAGER para já trazer os dados do cliente na resposta.
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    // Muitos agendamentos podem usar o mesmo serviço.
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "servico_id", nullable = false)
    private Servico servico;

    // Início do atendimento
    @Column(nullable = false)
    private LocalDateTime inicio;

    // Fim do atendimento — calculado a partir de inicio + duração do serviço
    @Column(nullable = false)
    private LocalDateTime fim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAgendamento status = StatusAgendamento.PENDENTE;

    // Observações opcionais (ex: "cliente pediu para chegar 10 min antes")
    @Column
    private String observacoes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    public void prePersist() {
        this.criadoEm = LocalDateTime.now();
        if (this.status == null) {
            this.status = StatusAgendamento.PENDENTE;
        }
    }

    // Construtor vazio obrigatório para o JPA
    public Agendamento() {
    }

    // --- Getters e Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Profissional getProfissional() {
        return profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Servico getServico() {
        return servico;
    }

    public void setServico(Servico servico) {
        this.servico = servico;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public void setInicio(LocalDateTime inicio) {
        this.inicio = inicio;
    }

    public LocalDateTime getFim() {
        return fim;
    }

    public void setFim(LocalDateTime fim) {
        this.fim = fim;
    }

    public StatusAgendamento getStatus() {
        return status;
    }

    public void setStatus(StatusAgendamento status) {
        this.status = status;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
}
