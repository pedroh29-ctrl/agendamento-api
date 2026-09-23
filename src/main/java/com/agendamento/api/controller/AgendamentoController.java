package com.agendamento.api.controller;

import com.agendamento.api.dto.AgendamentoRequest;
import com.agendamento.api.dto.ReagendarRequest;
import com.agendamento.api.dto.StatusRequest;
import com.agendamento.api.model.Agendamento;
import com.agendamento.api.model.StatusAgendamento;
import com.agendamento.api.service.AgendamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Agendamentos", description = "Agenda de compromissos do freelancer")
@RestController
@RequestMapping("/agendamentos")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    public AgendamentoController(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    // -----------------------------------------------------------------------
    // POST /agendamentos
    // Cria um agendamento a partir de clienteId, servicoId e início.
    // O service valida conflito de horário e datas no passado.
    // -----------------------------------------------------------------------
    @Operation(summary = "Criar agendamento",
            description = "Agenda um serviço para um cliente. Valida conflito de horário e datas passadas")
    @PostMapping
    public ResponseEntity<Agendamento> criar(@Valid @RequestBody AgendamentoRequest req) {
        Agendamento criado = agendamentoService.criar(
                req.getClienteId(), req.getServicoId(), req.getInicio(), req.getObservacoes());
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    // -----------------------------------------------------------------------
    // GET /agendamentos            → todos, em ordem cronológica
    // GET /agendamentos?status=... → filtra por status
    // -----------------------------------------------------------------------
    @Operation(summary = "Listar agendamentos",
            description = "Lista todos os agendamentos em ordem cronológica, ou filtra por status")
    @GetMapping
    public ResponseEntity<List<Agendamento>> listar(
            @RequestParam(required = false) StatusAgendamento status) {
        if (status != null) {
            return ResponseEntity.ok(agendamentoService.listarPorStatus(status));
        }
        return ResponseEntity.ok(agendamentoService.listarTodos());
    }

    // -----------------------------------------------------------------------
    // GET /agendamentos/periodo?de=2026-01-01T00:00:00&ate=2026-01-07T23:59:59
    // Agenda dentro de um intervalo (dia, semana, etc.).
    // -----------------------------------------------------------------------
    @Operation(summary = "Agenda por período",
            description = "Lista os agendamentos entre 'de' e 'ate' (formato ISO: 2026-01-01T09:00:00)")
    @GetMapping("/periodo")
    public ResponseEntity<List<Agendamento>> listarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime de,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime ate) {
        return ResponseEntity.ok(agendamentoService.listarPorPeriodo(de, ate));
    }

    @Operation(summary = "Buscar agendamento por ID")
    @GetMapping("/{id}")
    public ResponseEntity<Agendamento> buscarPorId(@PathVariable Long id) {
        return agendamentoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // -----------------------------------------------------------------------
    // PATCH /agendamentos/{id}/reagendar
    // Move o compromisso para um novo horário, revalidando conflitos.
    // -----------------------------------------------------------------------
    @Operation(summary = "Reagendar", description = "Move o agendamento para um novo horário de início")
    @PatchMapping("/{id}/reagendar")
    public ResponseEntity<Agendamento> reagendar(@PathVariable Long id,
            @Valid @RequestBody ReagendarRequest req) {
        return agendamentoService.reagendar(id, req.getInicio())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // -----------------------------------------------------------------------
    // PATCH /agendamentos/{id}/status
    // Altera o status respeitando as transições válidas.
    // -----------------------------------------------------------------------
    @Operation(summary = "Alterar status",
            description = "Confirma, conclui ou cancela um agendamento (respeitando transições válidas)")
    @PatchMapping("/{id}/status")
    public ResponseEntity<Agendamento> alterarStatus(@PathVariable Long id,
            @Valid @RequestBody StatusRequest req) {
        return agendamentoService.alterarStatus(id, req.getStatus())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Excluir agendamento")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (agendamentoService.deletar(id)) {
            return ResponseEntity.noContent().build(); // 204
        }
        return ResponseEntity.notFound().build(); // 404
    }
}
