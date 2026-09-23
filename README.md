# Agendamento API

API REST **multi-freelancer**: cada profissional tem a própria conta e gerencia seus serviços, clientes, agenda e horário de expediente — tudo isolado dos demais. Inclui validação de conflito de horário, controle de status e notificações ao cliente.

Feita em **Spring Boot 3.3 + Java 21**, começa com **H2 em memória** (zero configuração) e troca para **PostgreSQL** só mudando o perfil.

## Como rodar

```bash
mvn spring-boot:run
```

A API sobe em `http://localhost:8080` com o perfil `dev` (H2 em memória).

- **Swagger UI** (documentação interativa): http://localhost:8080/swagger-ui.html
- **Console H2** (ver o banco): http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:agendamentodb` — usuário: `sa` — sem senha

## Autenticação (multi-freelancer)

Cada freelancer cria a própria conta e faz login com e-mail + senha (HTTP Basic). A senha é guardada como hash BCrypt.

1. **Registrar** (público): `POST /profissionais/registrar`
2. Usar o e-mail e a senha no **HTTP Basic** em todas as outras chamadas.

Cada profissional só enxerga e manipula os próprios dados — a agenda de um nunca aparece para o outro.

## Conceitos

| Recurso           | O que é                                                        |
|-------------------|----------------------------------------------------------------|
| **Profissional**  | O freelancer dono da agenda. É também a conta de login.        |
| **Serviço**       | Um tipo de trabalho oferecido: nome, duração (min) e preço.    |
| **Cliente**       | Quem agenda: nome, e-mail e telefone.                          |
| **Agendamento**   | Um cliente reserva um serviço para um horário.                 |
| **Disponibilidade** | Faixa de expediente semanal (ex: MONDAY 09:00–18:00).        |

O horário de **fim** é calculado automaticamente (início + duração do serviço).

### Status de um agendamento

```
PENDENTE ──► CONFIRMADO ──► CONCLUIDO
   │              │
   └──────────────┴────────► CANCELADO
```

`CONCLUIDO` e `CANCELADO` são estados finais.

## Regras de negócio

- Não é possível agendar em uma data/hora no passado.
- **Conflito de horário**: dois agendamentos ativos do mesmo profissional não podem se sobrepor. Agendamentos `CANCELADO` liberam o horário.
- **Expediente**: o agendamento precisa caber dentro de uma faixa de disponibilidade do profissional no dia. Se o profissional não cadastrou nenhuma faixa, a agenda é considerada aberta.
- Cliente e serviço precisam existir e pertencer ao profissional logado.
- E-mail de cliente é único dentro da carteira de cada profissional.
- Transições de status inválidas são recusadas.
- O cliente é **notificado** (por enquanto via log) na criação, confirmação, cancelamento e reagendamento.

## Endpoints

### Profissionais — `/profissionais`
| Método | Rota                        | Descrição                          | Auth    |
|--------|-----------------------------|------------------------------------|---------|
| POST   | `/profissionais/registrar`  | Criar conta de freelancer          | Pública |
| GET    | `/profissionais/eu`         | Dados do profissional autenticado  | Sim     |

### Serviços — `/servicos`, Clientes — `/clientes`
CRUD completo (`POST`, `GET`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}`), sempre no escopo do profissional logado.

### Disponibilidade — `/disponibilidades`
| Método | Rota                   | Descrição                                |
|--------|------------------------|------------------------------------------|
| POST   | `/disponibilidades`    | Cadastrar faixa de expediente            |
| GET    | `/disponibilidades`    | Listar o expediente                      |
| DELETE | `/disponibilidades/{id}` | Remover faixa                          |

### Agendamentos — `/agendamentos`
| Método | Rota                          | Descrição                                  |
|--------|-------------------------------|--------------------------------------------|
| POST   | `/agendamentos`               | Criar agendamento                          |
| GET    | `/agendamentos`               | Listar todos (ordem cronológica)           |
| GET    | `/agendamentos?status=PENDENTE` | Filtrar por status                       |
| GET    | `/agendamentos/periodo?de=...&ate=...` | Agenda dentro de um período       |
| GET    | `/agendamentos/{id}`          | Buscar por ID                              |
| PATCH  | `/agendamentos/{id}/reagendar`| Mover para novo horário                    |
| PATCH  | `/agendamentos/{id}/status`   | Confirmar / concluir / cancelar            |
| DELETE | `/agendamentos/{id}`          | Excluir                                    |

## Exemplos rápidos

Registrar profissional:

```bash
curl -X POST http://localhost:8080/profissionais/registrar \
  -H "Content-Type: application/json" \
  -d '{"nome":"Ana Freela","email":"ana@free.com","senha":"senha123","profissao":"Cabeleireira"}'
```

Cadastrar expediente (segunda, 9h às 18h):

```bash
curl -u ana@free.com:senha123 -X POST http://localhost:8080/disponibilidades \
  -H "Content-Type: application/json" \
  -d '{"diaDaSemana":"MONDAY","horaInicio":"09:00","horaFim":"18:00"}'
```

Cadastrar serviço:

```bash
curl -u ana@free.com:senha123 -X POST http://localhost:8080/servicos \
  -H "Content-Type: application/json" \
  -d '{"nome":"Corte de cabelo","descricao":"Corte feminino","duracaoMinutos":45,"preco":50.00}'
```

Cadastrar cliente:

```bash
curl -u ana@free.com:senha123 -X POST http://localhost:8080/clientes \
  -H "Content-Type: application/json" \
  -d '{"nome":"Maria Silva","email":"maria@email.com","telefone":"11999998888"}'
```

Criar um agendamento (usa os IDs criados acima):

```bash
curl -u ana@free.com:senha123 -X POST http://localhost:8080/agendamentos \
  -H "Content-Type: application/json" \
  -d '{"clienteId":1,"servicoId":1,"inicio":"2026-10-05T09:00:00","observacoes":"primeira vez"}'
```

Confirmar o agendamento:

```bash
curl -u ana@free.com:senha123 -X PATCH http://localhost:8080/agendamentos/1/status \
  -H "Content-Type: application/json" \
  -d '{"status":"CONFIRMADO"}'
```

## Notificações

Por padrão, as notificações ao cliente são **registradas no log** (implementação `LogNotificacaoService`), sem depender de servidor SMTP.

Para enviar **e-mail de verdade**, crie uma implementação de `NotificacaoService` (ex: usando `JavaMailSender`), anote-a com `@Primary` e configure o SMTP em `application-prod.properties`. Nenhuma outra mudança de código é necessária.

## Testes

```bash
mvn test
```

Os testes de unidade cobrem as regras do `AgendamentoService`: conflito de horário, data no passado, fora do expediente e transições de status.

## Usar PostgreSQL (produção)

Em `src/main/resources/application.properties`, troque:

```properties
spring.profiles.active=prod
```

E ajuste host/usuário/senha em `application-prod.properties`. Nenhuma mudança de código é necessária.

## Respostas de erro

| Código | Quando                                                        |
|--------|---------------------------------------------------------------|
| 400    | Dados inválidos (campo vazio, data no passado, etc.)          |
| 401    | Sem autenticação (falta o HTTP Basic)                         |
| 404    | Recurso não encontrado (cliente/serviço/agendamento inexistente) |
| 409    | Conflito de regra (horário sobreposto, fora do expediente, e-mail duplicado, status inválido) |
