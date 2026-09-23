package com.agendamento.api.repository;

import com.agendamento.api.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// Todas as consultas são restritas ao profissional dono, isolando os dados
// de cada freelancer.
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findByProfissionalId(Long profissionalId);

    Optional<Cliente> findByIdAndProfissionalId(Long id, Long profissionalId);

    // Usado para impedir dois clientes com o mesmo e-mail dentro da carteira
    // de um mesmo profissional (o e-mail pode se repetir entre profissionais).
    boolean existsByEmailAndProfissionalId(String email, Long profissionalId);

    Optional<Cliente> findByEmailAndProfissionalId(String email, Long profissionalId);
}
