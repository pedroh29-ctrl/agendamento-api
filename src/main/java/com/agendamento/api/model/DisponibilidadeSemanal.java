package com.agendamento.api.model;

import jakarta.persistence.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

// Uma faixa de expediente do profissional em um dia da semana.
// Ex: SEGUNDA das 09:00 às 18:00. Um profissional pode ter várias faixas
// (inclusive mais de uma no mesmo dia, ex: manhã e tarde com pausa no almoço).
//
// Um agendamento só é aceito se couber inteiramente dentro de uma dessas faixas.
@Entity
@Table(name = "disponibilidades")
public class DisponibilidadeSemanal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profissional_id", nullable = false)
    private Profissional profissional;

    // Dia da semana desta faixa (MONDAY..SUNDAY).
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek diaDaSemana;

    @Column(nullable = false)
    private LocalTime horaInicio;

    @Column(nullable = false)
    private LocalTime horaFim;

    public DisponibilidadeSemanal() {
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
