package com.agendamento.api.service;

import com.agendamento.api.exception.RegraNegocioException;
import com.agendamento.api.model.DisponibilidadeSemanal;
import com.agendamento.api.model.Profissional;
import com.agendamento.api.repository.DisponibilidadeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

// Gerencia o horário de expediente (disponibilidade semanal) do profissional
// logado e valida se um agendamento cabe dentro dele.
@Service
public class DisponibilidadeService {

    private final DisponibilidadeRepository disponibilidadeRepository;
    private final ProfissionalAtualService profissionalAtual;

    public DisponibilidadeService(DisponibilidadeRepository disponibilidadeRepository,
                                  ProfissionalAtualService profissionalAtual) {
        this.disponibilidadeRepository = disponibilidadeRepository;
        this.profissionalAtual = profissionalAtual;
    }

    // Cadastra uma faixa de expediente (ex: SEGUNDA 09:00–18:00) para o
    // profissional logado, validando que o fim é depois do início.
    public DisponibilidadeSemanal criar(DisponibilidadeSemanal disponibilidade) {
        validarFaixa(disponibilidade);
        disponibilidade.setProfissional(profissionalAtual.obter());
        return disponibilidadeRepository.save(disponibilidade);
    }

    public List<DisponibilidadeSemanal> listar() {
        return disponibilidadeRepository.findByProfissionalId(profissionalAtual.obter().getId());
    }

    public boolean deletar(Long id) {
        Optional<DisponibilidadeSemanal> faixa = disponibilidadeRepository.findByIdAndProfissionalId(
                id, profissionalAtual.obter().getId());
        if (faixa.isPresent()) {
            disponibilidadeRepository.delete(faixa.get());
            return true;
        }
        return false;
    }

    // -----------------------------------------------------------------------
    // Valida se o intervalo [inicio, fim) de um agendamento cabe inteiramente
    // dentro de alguma faixa de expediente do profissional no dia da semana
    // correspondente.
    //
    // Se o profissional ainda não cadastrou nenhuma disponibilidade, a agenda
    // é considerada aberta (não bloqueia) — assim a validação não atrapalha
    // quem não quer configurar expediente.
    //
    // Não permite agendamentos que cruzem a virada do dia (início e fim em
    // dias diferentes), pois uma faixa semanal é sempre dentro de um mesmo dia.
    // -----------------------------------------------------------------------
    public void validarDentroDoExpediente(Long profissionalId, LocalDateTime inicio, LocalDateTime fim) {
        List<DisponibilidadeSemanal> todas = disponibilidadeRepository.findByProfissionalId(profissionalId);
        if (todas.isEmpty()) {
            return; // sem expediente configurado → agenda aberta
        }

        if (!inicio.toLocalDate().equals(fim.toLocalDate())) {
            throw new RegraNegocioException(
                    "O agendamento não pode cruzar a virada do dia");
        }

        List<DisponibilidadeSemanal> faixasDoDia = disponibilidadeRepository
                .findByProfissionalIdAndDiaDaSemana(profissionalId, inicio.getDayOfWeek());

        LocalTime ini = inicio.toLocalTime();
        LocalTime fimT = fim.toLocalTime();

        boolean cabe = faixasDoDia.stream().anyMatch(f ->
                !ini.isBefore(f.getHoraInicio()) && !fimT.isAfter(f.getHoraFim()));

        if (!cabe) {
            throw new RegraNegocioException(
                    "O horário está fora do expediente do profissional para " + inicio.getDayOfWeek());
        }
    }

    private void validarFaixa(DisponibilidadeSemanal faixa) {
        if (faixa.getDiaDaSemana() == null) {
            throw new RegraNegocioException("O dia da semana é obrigatório");
        }
        if (faixa.getHoraInicio() == null || faixa.getHoraFim() == null) {
            throw new RegraNegocioException("Hora de início e fim são obrigatórias");
        }
        if (!faixa.getHoraFim().isAfter(faixa.getHoraInicio())) {
            throw new RegraNegocioException("A hora de fim deve ser depois da hora de início");
        }
    }
}
