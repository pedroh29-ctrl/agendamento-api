package com.agendamento.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// @Configuration: esta classe define beans de configuração
// @EnableWebSecurity: ativa o controle de segurança do Spring
//
// A autenticação usa os profissionais gravados no banco
// (ver ProfissionalUserDetailsService). O e-mail é o usuário e a senha é
// verificada contra o hash BCrypt. O isolamento de dados (cada profissional
// só vê o seu) é aplicado na camada de service, não aqui.
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Desabilita CSRF para facilitar testes via Swagger/Postman
                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth

                        // Swagger e H2 console — sempre públicos
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/api-docs/**",
                                "/h2-console/**")
                        .permitAll()

                        // Registro de novo profissional — público (é como se cria a conta)
                        .requestMatchers(HttpMethod.POST, "/profissionais/registrar").permitAll()

                        // Todo o resto exige um profissional autenticado.
                        // Não há mais leitura pública: a agenda de cada um é privada.
                        .anyRequest().authenticated())

                // Autenticação básica (usuário = e-mail, senha via header HTTP)
                .httpBasic(Customizer.withDefaults())

                // Permite o H2 console (usa frames internamente)
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

    // BCrypt é o algoritmo de hash recomendado para senhas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
