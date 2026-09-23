package com.agendamento.api.service;

import com.agendamento.api.dto.RegistroProfissionalRequest;
import com.agendamento.api.exception.RegraNegocioException;
import com.agendamento.api.model.Profissional;
import com.agendamento.api.repository.ProfissionalRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ProfissionalService {

    private final ProfissionalRepository profissionalRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfissionalService(ProfissionalRepository profissionalRepository,
                               PasswordEncoder passwordEncoder) {
        this.profissionalRepository = profissionalRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Registra um novo profissional, impedindo e-mail duplicado e guardando a
    // senha como hash BCrypt (nunca em texto puro).
    public Profissional registrar(RegistroProfissionalRequest req) {
        if (profissionalRepository.existsByEmail(req.getEmail())) {
            throw new RegraNegocioException("Já existe um profissional com o e-mail " + req.getEmail());
        }

        Profissional profissional = new Profissional();
        profissional.setNome(req.getNome());
        profissional.setEmail(req.getEmail());
        profissional.setProfissao(req.getProfissao());
        profissional.setSenha(passwordEncoder.encode(req.getSenha()));

        return profissionalRepository.save(profissional);
    }
}
