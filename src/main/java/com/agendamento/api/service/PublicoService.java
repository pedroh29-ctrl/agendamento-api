package com.agendamento.api.service;

import com.agendamento.api.dto.AgendamentoPublicoRequest;
import com.agendamento.api.exception.RecursoNaoEncontradoException;
import com.agendamento.api.exception.RegraNegocioException;
import com.agendamento.api.model.Agendamento;
import com.agendamento.api.model.Cliente;
import com.agendamento.api.model.Profissional;
import com.agendamento.api.model.Servico;
import com.agendamento.api.model.StatusAgendamento;
import com.agendamento.api.repository.AgendamentoRepository;
import com.agendamento.api.repository.ClienteRepository;
import com.agendamento.api.repository.ProfissionalRepository;
import com.agendamento.api.repository.ServicoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

// Fluxo PÚBLICO de agendamento: o próprio cliente marca um horário sem login.
// Diferente do AgendamentoService (que age no escopo do profissional logado),
// aqui o profissional é escolhido pelo cliente. As mesmas regras de negócio
// (passado, expediente e conflito de horário) continuam valendo.
@Service
public class PublicoService {

    private final ProfissionalRepository profissionalRepository;
    private final ServicoRepository servicoRepository;
    private final ClienteRepository clienteRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final DisponibilidadeService disponibilidadeService;
    private final NotificacaoService notificacaoService;

    public PublicoService(ProfissionalRepository profissionalRepository,
                          ServicoRepository servicoRepository,
                          ClienteRepository clienteRepository,
                          AgendamentoRepository agendamentoRepository,
                          DisponibilidadeService disponibilidadeService,
                          NotificacaoService notificacaoService) {
        this.profissionalRepository = profissionalRepository;
        this.servicoRepository = servicoRepository;
        this.clienteRepository = clienteRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.disponibilidadeService = disponibilidadeService;
        this.notificacaoService = notificacaoService;
    }

    // Lista todos os profissionais disponíveis para agendamento.
    public List<Profissional> listarProfissionais() {
        return profissionalRepository.findAll();
    }

    // Lista os serviços de um profissional específico.
    public List<Servico> listarServicosDoProfissional(Long profissionalId) {
        // Garante que o profissional existe antes de listar.
        profissionalRepository.findById(profissionalId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Profissional não encontrado: " + profissionalId));
        return servicoRepository.findByProfissionalId(profissionalId);
    }

    // -----------------------------------------------------------------------
    // Cria um agendamento a partir dos dados informados pelo próprio cliente.
    //  1. Profissional e serviço precisam existir (e o serviço ser do profissional).
    //  2. O cliente é reaproveitado (se já existir com aquele e-mail na carteira
    //     do profissional) ou criado na hora.
    //  3. Valida passado, expediente e conflito de horário.
    // -----------------------------------------------------------------------
    public Agendamento agendar(AgendamentoPublicoRequest req) {
        Profissional profissional = profissionalRepository.findById(req.getProfissionalId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Profissional não encontrado: " + req.getProfissionalId()));

        Servico servico = servicoRepository.findByIdAndProfissionalId(
                        req.getServicoId(), profissional.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Serviço não encontrado para este profissional"));

        LocalDateTime inicio = req.getInicio();
        if (inicio == null) {
            throw new RegraNegocioException("O horário de início é obrigatório");
        }
        if (inicio.isBefore(LocalDateTime.now())) {
            throw new RegraNegocioException("Não é possível agendar em uma data/hora no passado");
        }

        LocalDateTime fim = inicio.plusMinutes(servico.getDuracaoMinutos());

        // Mesmas validações do fluxo interno.
        disponibilidadeService.validarDentroDoExpediente(profissional.getId(), inicio, fim);
        validarConflito(profissional.getId(), inicio, fim);

        // Reaproveita o cliente pelo e-mail dentro da carteira do profissional,
        // ou cria um novo com os dados informados.
        Cliente cliente = clienteRepository
                .findByEmailAndProfissionalId(req.getClienteEmail(), profissional.getId())
                .orElseGet(() -> {
                    Cliente novo = new Cliente(req.getClienteNome(), req.getClienteEmail(),
                            req.getClienteTelefone());
                    novo.setProfissional(profissional);
                    return clienteRepository.save(novo);
                });

        Agendamento agendamento = new Agendamento();
        agendamento.setProfissional(profissional);
        agendamento.setCliente(cliente);
        agendamento.setServico(servico);
        agendamento.setInicio(inicio);
        agendamento.setFim(fim);
        agendamento.setObservacoes(req.getObservacoes());
        agendamento.setStatus(StatusAgendamento.PENDENTE);

        Agendamento salvo = agendamentoRepository.save(agendamento);
        notificacaoService.notificarCriacao(salvo);
        return salvo;
    }

    private void validarConflito(Long profissionalId, LocalDateTime inicio, LocalDateTime fim) {
        List<Agendamento> conflitos = agendamentoRepository.encontrarConflitos(
                profissionalId, inicio, fim, null);
        if (!conflitos.isEmpty()) {
            throw new RegraNegocioException(
                    "Esse horário já está ocupado. Escolha outro.");
        }
    }
}
