package com.agendamento.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

// Dados para cadastrar uma faixa de expediente.
// Ex: { "diaDaSemana": "MONDAY", "horaInicio": "09:00", "horaFim": "18:00" }
public class DisponibilidadeRequest {

    @NotNull(message = "O dia da semana é obrigatório (MONDAY..SUNDAY)")
    private DayOfWeek diaDaSemana;

    @NotNull(message = "A hora de início é obrigatória")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime horaInicio;

    @NotNull(message = "A hora de fim é obrigatória")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime horaFim;

    public DayOfWeek getDiaDaSemana() {
        return diaDaSemana;
    }

    public void setDiaDaSemana(DayOfWeek diaDaSemana) {
        this.diaDaSemana = diaDaSemana;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFim() {
        return horaFim;
    }

    public void setHoraFim(LocalTime horaFim) {
        this.horaFim = horaFim;
    }
}
