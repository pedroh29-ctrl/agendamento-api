package com.agendamento.api.repository;

import com.agendamento.api.model.DisponibilidadeSemanal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface DisponibilidadeRepository extends JpaRepository<DisponibilidadeSemanal, Long> {

    // Todas as faixas de expediente de um profissional.
    List<DisponibilidadeSemanal> findByProfissionalId(Long profissionalId);

    // Faixas de um profissional em um dia específico da semana — usadas para
    // validar se um agendamento cabe no expediente.
    List<DisponibilidadeSemanal> findByProfissionalIdAndDiaDaSemana(
            Long profissionalId, DayOfWeek diaDaSemana);

    Optional<DisponibilidadeSemanal> findByIdAndProfissionalId(Long id, Long profissionalId);
}
