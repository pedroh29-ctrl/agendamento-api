package com.agendamento.api.controller;

import com.agendamento.api.dto.ProfissionalResponse;
import com.agendamento.api.dto.RegistroProfissionalRequest;
import com.agendamento.api.model.Profissional;
import com.agendamento.api.service.ProfissionalAtualService;
import com.agendamento.api.service.ProfissionalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Profissionais", description = "Cadastro e conta do freelancer")
@RestController
@RequestMapping("/profissionais")
public class ProfissionalController {

    private final ProfissionalService profissionalService;
    private final ProfissionalAtualService profissionalAtualService;

    public ProfissionalController(ProfissionalService profissionalService,
                                  ProfissionalAtualService profissionalAtualService) {
        this.profissionalService = profissionalService;
        this.profissionalAtualService = profissionalAtualService;
    }

    // -----------------------------------------------------------------------
    // POST /profissionais/registrar  (público)
    // Cria a conta do profissional. Depois disso, use o e-mail e a senha no
    // HTTP Basic para acessar os demais endpoints.
    // -----------------------------------------------------------------------
    @Operation(summary = "Registrar profissional",
            description = "Cria uma nova conta de freelancer (endpoint público)")
    @PostMapping("/registrar")
    public ResponseEntity<ProfissionalResponse> registrar(
            @Valid @RequestBody RegistroProfissionalRequest req) {
        Profissional criado = profissionalService.registrar(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ProfissionalResponse(criado));
    }

    // -----------------------------------------------------------------------
    // GET /profissionais/eu
    // Retorna os dados do profissional autenticado (perfil).
    // -----------------------------------------------------------------------
    @Operation(summary = "Meu perfil", description = "Dados do profissional autenticado")
    @GetMapping("/eu")
    public ResponseEntity<ProfissionalResponse> eu() {
        Profissional atual = profissionalAtualService.obter();
        return ResponseEntity.ok(new ProfissionalResponse(atual));
    }
}
