package com.agendamento.api.service;

import com.agendamento.api.exception.RecursoNaoEncontradoException;
import com.agendamento.api.model.Profissional;
import com.agendamento.api.repository.ProfissionalRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

// Ponte entre o Spring Security e o domínio: recupera a entidade Profissional
// correspondente ao usuário autenticado na requisição atual.
//
// Todos os services usam isto para saber "de quem" são os dados, garantindo
// que cada profissional só acesse a própria agenda, clientes e serviços.
@Service
public class ProfissionalAtualService {

    private final ProfissionalRepository profissionalRepository;

    public ProfissionalAtualService(ProfissionalRepository profissionalRepository) {
        this.profissionalRepository = profissionalRepository;
    }

    // Retorna o profissional logado. O username do Security é o e-mail.
    public Profissional obter() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RecursoNaoEncontradoException("Nenhum profissional autenticado");
        }
        String email = auth.getName();
        return profissionalRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Profissional autenticado não encontrado: " + email));
    }
}
