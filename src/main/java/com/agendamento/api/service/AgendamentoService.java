package com.agendamento.api.service;

import com.agendamento.api.exception.RecursoNaoEncontradoException;
import com.agendamento.api.exception.RegraNegocioException;
import com.agendamento.api.model.Agendamento;
import com.agendamento.api.model.Cliente;
import com.agendamento.api.model.Profissional;
import com.agendamento.api.model.Servico;
import com.agendamento.api.model.StatusAgendamento;
import com.agendamento.api.repository.AgendamentoRepository;
import com.agendamento.api.repository.ClienteRepository;
import com.agendamento.api.repository.ServicoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// Regras de negócio da agenda, sempre no escopo do profissional autenticado.
// Cada profissional só cria e enxerga os próprios agendamentos, usando apenas
// os próprios clientes e serviços.
@Service
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final ClienteRepository clienteRepository;
    private final ServicoRepository servicoRepository;
    private final ProfissionalAtualService profissionalAtual;
    private final DisponibilidadeService disponibilidadeService;
    private final NotificacaoService notificacaoService;

    public AgendamentoService(AgendamentoRepository agendamentoRepository,
            ClienteRepository clienteRepository,
            ServicoRepository servicoRepository,
            ProfissionalAtualService profissionalAtual,
            DisponibilidadeService disponibilidadeService,
            NotificacaoService notificacaoService) {
        this.agendamentoRepository = agendamentoRepository;
        this.clienteRepository = clienteRepository;
        this.servicoRepository = servicoRepository;
        this.profissionalAtual = profissionalAtual;
        this.disponibilidadeService = disponibilidadeService;
        this.notificacaoService = notificacaoService;
    }

    // -----------------------------------------------------------------------
    // Cria um novo agendamento aplicando todas as regras de negócio:
    // 1. Cliente e serviço precisam existir E pertencer ao profissional logado.
    // 2. O início não pode estar no passado.
    // 3. O fim é calculado como início + duração do serviço.
    // 4. O horário precisa estar dentro do expediente do profissional.
    // 5. Não pode haver conflito com outro agendamento ativo do profissional.
    // Status inicial é sempre PENDENTE. Ao final, o cliente é notificado.
    // -----------------------------------------------------------------------
    public Agendamento criar(Long clienteId, Long servicoId, LocalDateTime inicio, String observacoes) {
        Profissional dono = profissionalAtual.obter();

        Cliente cliente = clienteRepository.findByIdAndProfissionalId(clienteId, dono.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Cliente não encontrado: " + clienteId));

        Servico servico = servicoRepository.findByIdAndProfissionalId(servicoId, dono.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Serviço não encontrado: " + servicoId));

        if (inicio == null) {
            throw new RegraNegocioException("O horário de início é obrigatório");
        }
        if (inicio.isBefore(LocalDateTime.now())) {
            throw new RegraNegocioException("Não é possível agendar em uma data/hora no passado");
        }

        LocalDateTime fim = inicio.plusMinutes(servico.getDuracaoMinutos());

        // Precisa caber no expediente e não colidir com outro agendamento ativo.
        disponibilidadeService.validarDentroDoExpediente(dono.getId(), inicio, fim);
        validarConflito(dono.getId(), inicio, fim, null);

        Agendamento agendamento = new Agendamento();
        agendamento.setProfissional(dono);
        agendamento.setCliente(cliente);
        agendamento.setServico(servico);
        agendamento.setInicio(inicio);
        agendamento.setFim(fim);
        agendamento.setObservacoes(observacoes);
        agendamento.setStatus(StatusAgendamento.PENDENTE);

        Agendamento salvo = agendamentoRepository.save(agendamento);
        notificacaoService.notificarCriacao(salvo);
        return salvo;
    }

    public List<Agendamento> listarTodos() {
        return agendamentoRepository.findByProfissionalIdOrderByInicioAsc(
                profissionalAtual.obter().getId());
    }

    public List<Agendamento> listarPorStatus(StatusAgendamento status) {
        return agendamentoRepository.findByProfissionalIdAndStatusOrderByInicioAsc(
                profissionalAtual.obter().getId(), status);
    }

    // Lista a agenda dentro de um intervalo (ex: agenda de um dia ou semana).
    public List<Agendamento> listarPorPeriodo(LocalDateTime de, LocalDateTime ate) {
        if (de == null || ate == null) {
            throw new RegraNegocioException("Informe as datas 'de' e 'ate' para filtrar o período");
        }
        if (ate.isBefore(de)) {
            throw new RegraNegocioException("A data final não pode ser anterior à data inicial");
        }
        return agendamentoRepository.findByProfissionalIdAndInicioBetweenOrderByInicioAsc(
                profissionalAtual.obter().getId(), de, ate);
    }

    public Optional<Agendamento> buscarPorId(Long id) {
        return agendamentoRepository.findByIdAndProfissionalId(id, profissionalAtual.obter().getId());
    }

    // -----------------------------------------------------------------------
    // Reagenda um compromisso para um novo horário de início.
    // Recalcula o fim, revalida expediente e conflito (ignorando ele mesmo).
    // Não é permitido reagendar algo já CONCLUIDO ou CANCELADO.
    // -----------------------------------------------------------------------
    public Optional<Agendamento> reagendar(Long id, LocalDateTime novoInicio) {
        Profissional dono = profissionalAtual.obter();
        return agendamentoRepository.findByIdAndProfissionalId(id, dono.getId()).map(agendamento -> {
            if (agendamento.getStatus() == StatusAgendamento.CONCLUIDO
                    || agendamento.getStatus() == StatusAgendamento.CANCELADO) {
                throw new RegraNegocioException(
                        "Não é possível reagendar um agendamento " + agendamento.getStatus());
            }
            if (novoInicio == null) {
                throw new RegraNegocioException("O novo horário de início é obrigatório");
            }
            if (novoInicio.isBefore(LocalDateTime.now())) {
                throw new RegraNegocioException("Não é possível reagendar para uma data/hora no passado");
            }

            LocalDateTime novoFim = novoInicio.plusMinutes(agendamento.getServico().getDuracaoMinutos());
            disponibilidadeService.validarDentroDoExpediente(dono.getId(), novoInicio, novoFim);
            validarConflito(dono.getId(), novoInicio, novoFim, agendamento.getId());

            agendamento.setInicio(novoInicio);
            agendamento.setFim(novoFim);
            Agendamento salvo = agendamentoRepository.save(agendamento);
            notificacaoService.notificarReagendamento(salvo);
            return salvo;
        });
    }

    // -----------------------------------------------------------------------
    // Altera o status do agendamento respeitando transições válidas:
    // - CANCELADO e CONCLUIDO são estados finais (não podem mudar).
    // - PENDENTE → CONFIRMADO, CANCELADO
    // - CONFIRMADO → CONCLUIDO, CANCELADO
    // Notifica o cliente em confirmações e cancelamentos.
    // -----------------------------------------------------------------------
    public Optional<Agendamento> alterarStatus(Long id, StatusAgendamento novoStatus) {
        Profissional dono = profissionalAtual.obter();
        return agendamentoRepository.findByIdAndProfissionalId(id, dono.getId()).map(agendamento -> {
            StatusAgendamento atual = agendamento.getStatus();

            if (atual == StatusAgendamento.CANCELADO || atual == StatusAgendamento.CONCLUIDO) {
                throw new RegraNegocioException(
                        "Não é possível alterar um agendamento que já está " + atual);
            }

            boolean transicaoValida = switch (atual) {
                case PENDENTE -> novoStatus == StatusAgendamento.CONFIRMADO
                        || novoStatus == StatusAgendamento.CANCELADO;
                case CONFIRMADO -> novoStatus == StatusAgendamento.CONCLUIDO
                        || novoStatus == StatusAgendamento.CANCELADO;
                default -> false;
            };

            if (!transicaoValida) {
                throw new RegraNegocioException(
                        "Transição de status inválida: " + atual + " → " + novoStatus);
            }

            agendamento.setStatus(novoStatus);
            Agendamento salvo = agendamentoRepository.save(agendamento);

            if (novoStatus == StatusAgendamento.CONFIRMADO) {
                notificacaoService.notificarConfirmacao(salvo);
            } else if (novoStatus == StatusAgendamento.CANCELADO) {
                notificacaoService.notificarCancelamento(salvo);
            }
            return salvo;
        });
    }

    public boolean deletar(Long id) {
        Optional<Agendamento> agendamento = agendamentoRepository.findByIdAndProfissionalId(
                id, profissionalAtual.obter().getId());
        if (agendamento.isPresent()) {
            agendamentoRepository.delete(agendamento.get());
            return true;
        }
        return false;
    }

    // Lança RegraNegocioException se o intervalo [inicio, fim) colidir com
    // algum agendamento ativo do profissional (ignorando o próprio, ao reagendar).
    private void validarConflito(Long profissionalId, LocalDateTime inicio,
            LocalDateTime fim, Long idIgnorado) {
        List<Agendamento> conflitos = agendamentoRepository.encontrarConflitos(
                profissionalId, inicio, fim, idIgnorado);
        if (!conflitos.isEmpty()) {
            Agendamento c = conflitos.get(0);
            throw new RegraNegocioException(
                    "Conflito de horário: já existe um agendamento entre "
                            + c.getInicio() + " e " + c.getFim());
        }
    }
}
