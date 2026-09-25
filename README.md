# 📅 Agendamento API

> API REST para freelancers gerenciarem sua agenda: clientes, serviços, horário de expediente e agendamentos — com controle de conflito de horário, autenticação e isolamento total de dados entre profissionais.

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-blue)
![Docker](https://img.shields.io/badge/Docker-ready-blue)
![License](https://img.shields.io/badge/status-online-success)

## 🚀 Demo ao vivo

**🖥️ Aplicação web (interface):** https://pedroh29-ctrl.github.io/agendamento-web/
**⚙️ API (Swagger):** https://agendamento-api-i14n.onrender.com/swagger-ui.html
**💻 Código do frontend:** https://github.com/pedroh29-ctrl/agendamento-web

Use a **aplicação web** para criar sua conta, cadastrar clientes e serviços e montar a agenda pelo navegador — sem escrever nenhum comando. Ou explore a **API** direto pelo Swagger.

> Hospedada no plano gratuito do Render: no primeiro acesso após um período ocioso, pode levar ~30s para "acordar".

## ✨ Destaques

- **Multi-freelancer com isolamento de dados** — cada profissional só enxerga a própria agenda, clientes e serviços. Um nunca acessa os dados do outro.
- **Autenticação** via Spring Security (HTTP Basic), com senhas em hash BCrypt.
- **Regras de negócio reais** — conflito de horário, validação de expediente, agendamento no passado, máquina de estados de status.
- **Documentação interativa** com Swagger/OpenAPI.
- **Testes automatizados** das regras de negócio (JUnit + Mockito).
- **Pronto para produção** — Docker + PostgreSQL, configuração por variáveis de ambiente, deploy contínuo a cada push.

## 🛠️ Tecnologias

Java 21 · Spring Boot 3.3 (Web, Data JPA, Security, Validation) · PostgreSQL · H2 · Hibernate · Swagger/OpenAPI · JUnit 5 · Mockito · Maven · Docker

## 🧩 Domínio

| Recurso           | O que é                                                        |
|-------------------|----------------------------------------------------------------|
| **Profissional**  | O freelancer dono da agenda. É também a conta de login.        |
| **Serviço**       | Um tipo de trabalho oferecido: nome, duração (min) e preço.    |
| **Cliente**       | Quem agenda: nome, e-mail e telefone.                          |
| **Agendamento**   | Um cliente reserva um serviço para um horário.                 |
| **Disponibilidade** | Faixa de expediente semanal (ex: MONDAY 09:00–18:00).        |

O horário de **fim** é calculado automaticamente (início + duração do serviço).

### Ciclo de vida de um agendamento

```
PENDENTE ──► CONFIRMADO ──► CONCLUIDO
   │              │
   └──────────────┴────────► CANCELADO
```

`CONCLUIDO` e `CANCELADO` são estados finais.

## 🔒 Regras de negócio

- Não é possível agendar em uma data/hora no passado.
- **Conflito de horário**: dois agendamentos ativos do mesmo profissional não podem se sobrepor. Agendamentos `CANCELADO` liberam o horário.
- **Expediente**: o agendamento precisa caber dentro de uma faixa de disponibilidade do profissional. Sem faixa cadastrada, a agenda é considerada aberta.
- Cliente e serviço precisam existir e pertencer ao profissional logado.
- E-mail de cliente é único dentro da carteira de cada profissional.
- Transições de status inválidas são recusadas.
- O cliente é **notificado** (via log, com ponto de extensão para e-mail) em cada mudança.

## 📚 Endpoints

### Profissionais — `/profissionais`
| Método | Rota                        | Descrição                          | Auth    |
|--------|-----------------------------|------------------------------------|---------|
| POST   | `/profissionais/registrar`  | Criar conta de freelancer          | Pública |
| GET    | `/profissionais/eu`         | Dados do profissional autenticado  | Sim     |

### Serviços — `/servicos` · Clientes — `/clientes`
CRUD completo (`POST`, `GET`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}`), sempre no escopo do profissional logado.

### Disponibilidade — `/disponibilidades`
| Método | Rota                     | Descrição                     |
|--------|--------------------------|-------------------------------|
| POST   | `/disponibilidades`      | Cadastrar faixa de expediente |
| GET    | `/disponibilidades`      | Listar o expediente           |
| DELETE | `/disponibilidades/{id}` | Remover faixa                 |

### Agendamentos — `/agendamentos`
| Método | Rota                                    | Descrição                        |
|--------|-----------------------------------------|----------------------------------|
| POST   | `/agendamentos`                         | Criar agendamento                |
| GET    | `/agendamentos`                         | Listar todos (ordem cronológica) |
| GET    | `/agendamentos?status=PENDENTE`         | Filtrar por status               |
| GET    | `/agendamentos/periodo?de=...&ate=...`  | Agenda dentro de um período      |
| GET    | `/agendamentos/{id}`                    | Buscar por ID                    |
| PATCH  | `/agendamentos/{id}/reagendar`          | Mover para novo horário          |
| PATCH  | `/agendamentos/{id}/status`             | Confirmar / concluir / cancelar  |
| DELETE | `/agendamentos/{id}`                    | Excluir                          |

## ▶️ Como rodar localmente

Requisitos: Java 21 e Maven.

```bash
git clone https://github.com/pedroh29-ctrl/agendamento-api.git
cd agendamento-api
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`.

- Swagger: http://localhost:8080/swagger-ui.html
- Console H2: http://localhost:8080/h2-console

### Perfis de execução

| Perfil | Banco                        | Uso                                  |
|--------|------------------------------|--------------------------------------|
| `file` | H2 em arquivo (persistente)  | Padrão local — dados salvos em disco |
| `dev`  | H2 em memória (descartável)  | Testes rápidos                       |
| `prod` | PostgreSQL                   | Produção / deploy                    |

## 🐳 Deploy

O projeto inclui um `Dockerfile` (build multi-stage) e roda em qualquer plataforma com Docker. Em produção, as credenciais do banco são lidas de variáveis de ambiente:

```
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
```

Deploy contínuo: cada `git push` na branch `main` dispara um novo deploy automaticamente.

## 🧪 Testes

```bash
mvn test
```

Cobrem as regras do `AgendamentoService`: conflito de horário, data no passado, fora do expediente e transições de status válidas/inválidas.

## 📋 Exemplos (cURL)

Registrar profissional:

```bash
curl -X POST https://agendamento-api-i14n.onrender.com/profissionais/registrar \
  -H "Content-Type: application/json" \
  -d '{"nome":"Ana Freela","email":"ana@exemplo.com","senha":"senha123","profissao":"Cabeleireira"}'
```

Cadastrar serviço (autenticado):

```bash
curl -u ana@exemplo.com:senha123 -X POST https://agendamento-api-i14n.onrender.com/servicos \
  -H "Content-Type: application/json" \
  -d '{"nome":"Corte de cabelo","descricao":"Corte feminino","duracaoMinutos":45,"preco":50.00}'
```

Criar um agendamento:

```bash
curl -u ana@exemplo.com:senha123 -X POST https://agendamento-api-i14n.onrender.com/agendamentos \
  -H "Content-Type: application/json" \
  -d '{"clienteId":1,"servicoId":1,"inicio":"2026-12-01T09:00:00"}'
```

## ⚠️ Respostas de erro

| Código | Quando                                                        |
|--------|---------------------------------------------------------------|
| 400    | Dados inválidos (campo vazio, data no passado, etc.)          |
| 401    | Sem autenticação                                              |
| 404    | Recurso não encontrado                                        |
| 409    | Conflito de regra (horário sobreposto, fora do expediente, e-mail duplicado, status inválido) |

---

Desenvolvido por [pedroh29-ctrl](https://github.com/pedroh29-ctrl) 👨‍💻
