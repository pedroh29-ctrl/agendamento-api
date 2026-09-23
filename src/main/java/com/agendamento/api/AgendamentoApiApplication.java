package com.agendamento.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @SpringBootApplication combina três anotações:
// - @Configuration: esta classe pode definir beans
// - @EnableAutoConfiguration: Spring configura automaticamente com base nas dependências
// - @ComponentScan: escaneia os pacotes em busca de @Component, @Service, @Controller, etc.
@SpringBootApplication
public class AgendamentoApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgendamentoApiApplication.class, args);
    }
}
