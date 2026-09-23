package com.agendamento.api.repository;

import com.agendamento.api.model.Agendamento;
import com.agendamento.api.model.StatusAgendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

        // Todas as consultas são filtradas pelo profissional dono, garantindo que
        // cada freelancer só enxergue e manipule a própria agenda.

        // Agenda completa de um profissional, em ordem cronológica.
        List<Agendamento> findByProfissionalIdOrderByInicioAsc(Long profissionalId);

        // Agenda de um profissional filtrada por status (ex: só os PENDENTES).
        List<Agendamento> findByProfissionalIdAndStatusOrderByInicioAsc(
                        Long profissionalId, StatusAgendamento status);

        // Agenda de um profissional dentro de um intervalo (dia, semana, etc.).
        List<Agendamento> findByProfissionalIdAndInicioBetweenOrderByInicioAsc(
                        Long profissionalId, LocalDateTime de, LocalDateTime ate);

        // Busca por ID já restrita ao profissional dono (evita acessar agenda alheia).
        Optional<Agendamento> findByIdAndProfissionalId(Long id, Long profissionalId);

        // -----------------------------------------------------------------------
        // Detecção de conflito de horário, restrita à agenda do profissional.
        //
        // Dois intervalos [inicio, fim) se sobrepõem quando:
        // novoInicio < fimExistente E novoFim > inicioExistente
        //
        // Agendamentos CANCELADOS são ignorados (o horário deles ficou livre).
        // O parâmetro idIgnorado permite excluir o próprio agendamento da checagem
        // ao reagendar (passe null quando estiver criando um novo).
        // -----------------------------------------------------------------------
        @Query("""
                        SELECT a FROM Agendamento a
                        WHERE a.profissional.id = :profissionalId
                          AND a.status <> com.agendamento.api.model.StatusAgendamento.CANCELADO
                          AND (:idIgnorado IS NULL OR a.id <> :idIgnorado)
                          AND a.inicio < :fim
                          AND a.fim > :inicio
                        """)
        List<Agendamento> encontrarConflitos(
                        @Param("profissionalId") Long profissionalId,
                        @Param("inicio") LocalDateTime inicio,
                        @Param("fim") LocalDateTime fim,
                        @Param("idIgnorado") Long idIgnorado);
}
