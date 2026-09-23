package com.agendamento.api.config;

import com.agendamento.api.model.Profissional;
import com.agendamento.api.repository.ProfissionalRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// Ensina o Spring Security a autenticar usando os profissionais gravados no
// banco: o e-mail é o nome de usuário e a senha comparada é o hash BCrypt.
@Service
public class ProfissionalUserDetailsService implements UserDetailsService {

    private final ProfissionalRepository profissionalRepository;

    public ProfissionalUserDetailsService(ProfissionalRepository profissionalRepository) {
        this.profissionalRepository = profissionalRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Profissional profissional = profissionalRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Profissional não encontrado: " + email));

        // Todos os profissionais têm o mesmo papel; o isolamento de dados é
        // feito por profissionalId, não por role.
        return User.builder()
                .username(profissional.getEmail())
                .password(profissional.getSenha())
                .roles("PROFISSIONAL")
                .build();
    }
}
