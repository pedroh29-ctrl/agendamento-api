package com.agendamento.api.repository;

import com.agendamento.api.model.Profissional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfissionalRepository extends JpaRepository<Profissional, Long> {

    // Usado no login (o e-mail é o nome de usuário) e para impedir cadastro
    // de dois profissionais com o mesmo e-mail.
    Optional<Profissional> findByEmail(String email);

    boolean existsByEmail(String email);
}
