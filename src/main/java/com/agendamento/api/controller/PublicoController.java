package com.agendamento.api.controller;

import com.agendamento.api.dto.AgendamentoPublicoRequest;
import com.agendamento.api.dto.AgendamentoPublicoResponse;
import com.agendamento.api.dto.ProfissionalPublicoResponse;
import com.agendamento.api.dto.ServicoPublicoResponse;
import com.agendamento.api.model.Agendamento;
import com.agendamento.api.service.PublicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// Endpoints PÚBLICOS (sem login) para o cliente marcar um horário sozinho.
// São liberados no SecurityConfig sob o caminho /publico/**.
@Tag(name = "Público", description = "Agendamento pelo próprio cliente, sem login")
@RestController
@RequestMapping("/publico")
public class PublicoController {

    private final PublicoService publicoService;

    public PublicoController(PublicoService publicoService) {
        this.publicoService = publicoService;
    }

    // Lista os profissionais disponíveis para agendamento.
    @Operation(summary = "Listar profissionais", description = "Profissionais disponíveis para agendamento")
    @GetMapping("/profissionais")
    public ResponseEntity<List<ProfissionalPublicoResponse>> listarProfissionais() {
        List<ProfissionalPublicoResponse> lista = publicoService.listarProfissionais().stream()
                .map(ProfissionalPublicoResponse::new)
                .toList();
        return ResponseEntity.ok(lista);
    }

    // Lista os serviços oferecidos por um profissional.
    @Operation(summary = "Listar serviços do profissional")
    @GetMapping("/profissionais/{id}/servicos")
    public ResponseEntity<List<ServicoPublicoResponse>> listarServicos(@PathVariable Long id) {
        List<ServicoPublicoResponse> lista = publicoService.listarServicosDoProfissional(id).stream()
                .map(ServicoPublicoResponse::new)
                .toList();
        return ResponseEntity.ok(lista);
    }

    // Lista os horários livres de um profissional/serviço numa data.
    // Ex: GET /publico/horarios?profissionalId=1&servicoId=2&data=2026-12-15
    @Operation(summary = "Horários disponíveis", description = "Lista os horários livres de um profissional/serviço em uma data")
    @GetMapping("/horarios")
    public ResponseEntity<List<LocalDateTime>> horarios(
            @RequestParam Long profissionalId,
            @RequestParam Long servicoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(
                publicoService.horariosDisponiveis(profissionalId, servicoId, data));
    }

    // Cria um agendamento a partir dos dados do próprio cliente.
    @Operation(summary = "Marcar horário", description = "O cliente marca um horário informando seus dados. Valida conflito e expediente")
    @PostMapping("/agendamentos")
    public ResponseEntity<AgendamentoPublicoResponse> agendar(
            @Valid @RequestBody AgendamentoPublicoRequest req) {
        Agendamento criado = publicoService.agendar(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(new AgendamentoPublicoResponse(criado));
    }
}
