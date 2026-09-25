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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

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
                // Habilita CORS (usa o bean corsConfigurationSource abaixo).
                // Sem isto, o navegador bloqueia as chamadas vindas do frontend.
                .cors(Customizer.withDefaults())

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

                        // Fluxo público de agendamento (cliente marca sem login)
                        .requestMatchers("/publico/**").permitAll()

                        // Todo o resto exige um profissional autenticado.
                        // Não há mais leitura pública: a agenda de cada um é privada.
                        .anyRequest().authenticated())

                // Autenticação básica (usuário = e-mail, senha via header HTTP)
                .httpBasic(Customizer.withDefaults())

                // Permite o H2 console (usa frames internamente)
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

    // -----------------------------------------------------------------------
    // Configuração de CORS: define de quais origens (sites) o navegador pode
    // chamar esta API. O frontend roda em outro endereço, então precisa estar
    // liberado aqui.
    //
    // Usamos allowedOriginPatterns com "*" para aceitar qualquer origem
    // (prático para um projeto de portfólio/demo). Em um sistema real e
    // sensível, o ideal é restringir à(s) URL(s) específica(s) do frontend.
    // -----------------------------------------------------------------------
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // BCrypt é o algoritmo de hash recomendado para senhas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
