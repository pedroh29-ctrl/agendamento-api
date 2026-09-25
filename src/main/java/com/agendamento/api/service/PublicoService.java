package com.agendamento.api.service;

import com.agendamento.api.dto.AgendamentoPublicoRequest;
import com.agendamento.api.exception.RecursoNaoEncontradoException;
import com.agendamento.api.exception.RegraNegocioException;
import com.agendamento.api.model.Agendamento;
import com.agendamento.api.model.Cliente;
import com.agendamento.api.model.Profissional;
import com.agendamento.api.model.Servico;
import com.agendamento.api.model.StatusAgendamento;
import com.agendamento.api.model.DisponibilidadeSemanal;
import com.agendamento.api.repository.AgendamentoRepository;
import com.agendamento.api.repository.ClienteRepository;
import com.agendamento.api.repository.DisponibilidadeRepository;
import com.agendamento.api.repository.ProfissionalRepository;
import com.agendamento.api.repository.ServicoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

// Fluxo PÚBLICO de agendamento: o próprio cliente marca um horário sem login.
// Diferente do AgendamentoService (que age no escopo do profissional logado),
// aqui o profissional é escolhido pelo cliente. As mesmas regras de negócio
// (passado, expediente e conflito de horário) continuam valendo.
@Service
public class PublicoService {

    // Horário padrão de atendimento usado quando o profissional NÃO cadastrou
    // nenhuma faixa de expediente.
    private static final LocalTime PADRAO_INICIO = LocalTime.of(8, 0);
    private static final LocalTime PADRAO_FIM = LocalTime.of(18, 0);

    private final ProfissionalRepository profissionalRepository;
    private final ServicoRepository servicoRepository;
    private final ClienteRepository clienteRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final DisponibilidadeRepository disponibilidadeRepository;
    private final DisponibilidadeService disponibilidadeService;
    private final NotificacaoService notificacaoService;

    public PublicoService(ProfissionalRepository profissionalRepository,
            ServicoRepository servicoRepository,
            ClienteRepository clienteRepository,
            AgendamentoRepository agendamentoRepository,
            DisponibilidadeRepository disponibilidadeRepository,
            DisponibilidadeService disponibilidadeService,
            NotificacaoService notificacaoService) {
        this.profissionalRepository = profissionalRepository;
        this.servicoRepository = servicoRepository;
        this.clienteRepository = clienteRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.disponibilidadeRepository = disponibilidadeRepository;
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
    // 1. Profissional e serviço precisam existir (e o serviço ser do profissional).
    // 2. O cliente é reaproveitado (se já existir com aquele e-mail na carteira
    // do profissional) ou criado na hora.
    // 3. Valida passado, expediente e conflito de horário.
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

    // -----------------------------------------------------------------------
    // Calcula os horários LIVRES de um profissional para um serviço em uma data.
    //
    // Lógica:
    // 1. Descobre as faixas de expediente do profissional naquele dia da semana.
    // Se ele não cadastrou nenhuma faixa (em nenhum dia), usa o padrão 8h-18h.
    // 2. Divide cada faixa em fatias do tamanho da duração do serviço.
    // 3. Descarta as fatias que já passaram (quando a data é hoje).
    // 4. Descarta as fatias que colidem com algum agendamento ativo.
    // Devolve a lista de horários de início disponíveis (LocalDateTime).
    // -----------------------------------------------------------------------
    public List<LocalDateTime> horariosDisponiveis(Long profissionalId, Long servicoId, LocalDate data) {
        Profissional profissional = profissionalRepository.findById(profissionalId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Profissional não encontrado: " + profissionalId));

        Servico servico = servicoRepository.findByIdAndProfissionalId(servicoId, profissionalId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Serviço não encontrado para este profissional"));

        if (data == null) {
            throw new RegraNegocioException("Informe a data");
        }

        int duracao = servico.getDuracaoMinutos();

        // Faixas de expediente para o dia da semana da data pedida.
        List<DisponibilidadeSemanal> faixasDoDia = disponibilidadeRepository
                .findByProfissionalIdAndDiaDaSemana(profissionalId, data.getDayOfWeek());

        // Se o profissional não tem NENHUMA faixa cadastrada (em dia algum),
        // usamos o horário padrão. Se ele tem faixas mas nenhuma neste dia,
        // significa que ele não atende neste dia → sem horários.
        boolean temAlgumExpediente = !disponibilidadeRepository
                .findByProfissionalId(profissionalId).isEmpty();

        List<LocalTime[]> intervalos = new ArrayList<>();
        if (!temAlgumExpediente) {
            intervalos.add(new LocalTime[] { PADRAO_INICIO, PADRAO_FIM });
        } else {
            for (DisponibilidadeSemanal f : faixasDoDia) {
                intervalos.add(new LocalTime[] { f.getHoraInicio(), f.getHoraFim() });
            }
        }

        LocalDateTime agora = LocalDateTime.now();
        List<LocalDateTime> livres = new ArrayList<>();

        for (LocalTime[] intervalo : intervalos) {
            LocalDateTime slot = LocalDateTime.of(data, intervalo[0]);
            LocalDateTime fimFaixa = LocalDateTime.of(data, intervalo[1]);

            // Gera fatias enquanto o serviço couber inteiro dentro da faixa.
            while (!slot.plusMinutes(duracao).isAfter(fimFaixa)) {
                LocalDateTime fimSlot = slot.plusMinutes(duracao);

                boolean noPassado = slot.isBefore(agora);
                boolean ocupado = !agendamentoRepository
                        .encontrarConflitos(profissionalId, slot, fimSlot, null).isEmpty();

                if (!noPassado && !ocupado) {
                    livres.add(slot);
                }
                slot = fimSlot;
            }
        }

        return livres;
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
