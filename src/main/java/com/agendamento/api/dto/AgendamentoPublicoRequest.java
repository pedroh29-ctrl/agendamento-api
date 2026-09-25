package com.agendamento.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

// Dados enviados pelo próprio cliente ao marcar um horário na página pública.
// Ele informa com qual profissional e serviço quer marcar, seus próprios
// dados de contato e o horário desejado. Não há login.
public class AgendamentoPublicoRequest {

    @NotNull(message = "O profissionalId é obrigatório")
    private Long profissionalId;

    @NotNull(message = "O servicoId é obrigatório")
    private Long servicoId;

    @NotBlank(message = "Informe seu nome")
    @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
    private String clienteNome;

    @NotBlank(message = "Informe seu e-mail")
    @Email(message = "E-mail inválido")
    private String clienteEmail;

    @NotBlank(message = "Informe seu telefone")
    @Size(min = 8, max = 20, message = "O telefone deve ter entre 8 e 20 caracteres")
    private String clienteTelefone;

    @NotNull(message = "Escolha o horário")
    @Future(message = "O horário deve estar no futuro")
    private LocalDateTime inicio;

    private String observacoes;

    // --- Getters e Setters ---

    public Long getProfissionalId() {
        return profissionalId;
    }

    public void setProfissionalId(Long profissionalId) {
        this.profissionalId = profissionalId;
    }

    public Long getServicoId() {
        return servicoId;
    }

    public void setServicoId(Long servicoId) {
        this.servicoId = servicoId;
    }

    public String getClienteNome() {
        return clienteNome;
    }

    public void setClienteNome(String clienteNome) {
        this.clienteNome = clienteNome;
    }

    public String getClienteEmail() {
        return clienteEmail;
    }

    public void setClienteEmail(String clienteEmail) {
        this.clienteEmail = clienteEmail;
    }

    public String getClienteTelefone() {
        return clienteTelefone;
    }

    public void setClienteTelefone(String clienteTelefone) {
        this.clienteTelefone = clienteTelefone;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public void setInicio(LocalDateTime inicio) {
        this.inicio = inicio;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}
