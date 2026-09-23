package com.agendamento.api.controller;

import com.agendamento.api.dto.DisponibilidadeRequest;
import com.agendamento.api.model.DisponibilidadeSemanal;
import com.agendamento.api.service.DisponibilidadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Disponibilidade", description = "Horário de expediente do profissional")
@RestController
@RequestMapping("/disponibilidades")
public class DisponibilidadeController {

    private final DisponibilidadeService disponibilidadeService;

    public DisponibilidadeController(DisponibilidadeService disponibilidadeService) {
        this.disponibilidadeService = disponibilidadeService;
    }

    @Operation(summary = "Cadastrar faixa de expediente",
            description = "Ex: MONDAY das 09:00 às 18:00. Sem nenhuma faixa cadastrada, a agenda é considerada aberta")
    @PostMapping
    public ResponseEntity<DisponibilidadeSemanal> criar(@Valid @RequestBody DisponibilidadeRequest req) {
        DisponibilidadeSemanal faixa = new DisponibilidadeSemanal();
        faixa.setDiaDaSemana(req.getDiaDaSemana());
        faixa.setHoraInicio(req.getHoraInicio());
        faixa.setHoraFim(req.getHoraFim());
        DisponibilidadeSemanal criada = disponibilidadeService.criar(faixa);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @Operation(summary = "Listar expediente", description = "Faixas de expediente do profissional logado")
    @GetMapping
    public ResponseEntity<List<DisponibilidadeSemanal>> listar() {
        return ResponseEntity.ok(disponibilidadeService.listar());
    }

    @Operation(summary = "Remover faixa de expediente")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (disponibilidadeService.deletar(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
