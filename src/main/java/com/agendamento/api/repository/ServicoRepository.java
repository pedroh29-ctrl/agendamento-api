package com.agendamento.api.repository;

import com.agendamento.api.model.Servico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// Consultas restritas ao profissional dono, isolando o catálogo de serviços
// de cada freelancer.
@Repository
public interface ServicoRepository extends JpaRepository<Servico, Long> {

    List<Servico> findByProfissionalId(Long profissionalId);

    Optional<Servico> findByIdAndProfissionalId(Long id, Long profissionalId);
}
