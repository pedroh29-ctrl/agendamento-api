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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Testes de unidade das regras de negócio do AgendamentoService, isolando o
// service dos repositórios e serviços colaboradores com Mockito.
@ExtendWith(MockitoExtension.class)
@DisplayName("AgendamentoService — regras de negócio")
class AgendamentoServiceTest {

    @Mock
    private AgendamentoRepository agendamentoRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private ServicoRepository servicoRepository;
    @Mock
    private ProfissionalAtualService profissionalAtual;
    @Mock
    private DisponibilidadeService disponibilidadeService;
    @Mock
    private NotificacaoService notificacaoService;

    @InjectMocks
    private AgendamentoService service;

    private Profissional dono;
    private Cliente cliente;
    private Servico servico;

    @BeforeEach
    void setUp() {
        dono = new Profissional();
        dono.setId(1L);
        dono.setEmail("free@lancer.com");

        cliente = new Cliente("Maria", "maria@email.com", "11999998888");
        cliente.setId(10L);
        cliente.setProfissional(dono);

        servico = new Servico("Corte", "Corte de cabelo", 60, new BigDecimal("50.00"));
        servico.setId(20L);
        servico.setProfissional(dono);

        // A maioria dos testes precisa do profissional logado.
        lenient().when(profissionalAtual.obter()).thenReturn(dono);
    }

    // ----- criar() -----

    @Test
    @DisplayName("cria agendamento válido, calcula o fim e notifica")
    void criaAgendamentoValido() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        when(clienteRepository.findByIdAndProfissionalId(10L, 1L)).thenReturn(Optional.of(cliente));
        when(servicoRepository.findByIdAndProfissionalId(20L, 1L)).thenReturn(Optional.of(servico));
        when(agendamentoRepository.encontrarConflitos(eq(1L), any(), any(), any()))
                .thenReturn(List.of());
        when(agendamentoRepository.save(any(Agendamento.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Agendamento resultado = service.criar(10L, 20L, inicio, "obs");

        assertThat(resultado.getInicio()).isEqualTo(inicio);
        assertThat(resultado.getFim()).isEqualTo(inicio.plusMinutes(60)); // fim = início + duração
        assertThat(resultado.getStatus()).isEqualTo(StatusAgendamento.PENDENTE);
        assertThat(resultado.getProfissional()).isEqualTo(dono);
        verify(disponibilidadeService).validarDentroDoExpediente(eq(1L), eq(inicio), eq(inicio.plusMinutes(60)));
        verify(notificacaoService).notificarCriacao(resultado);
    }

    @Test
    @DisplayName("recusa agendamento no passado")
    void recusaAgendamentoNoPassado() {
        LocalDateTime passado = LocalDateTime.now().minusHours(1);
        when(clienteRepository.findByIdAndProfissionalId(10L, 1L)).thenReturn(Optional.of(cliente));
        when(servicoRepository.findByIdAndProfissionalId(20L, 1L)).thenReturn(Optional.of(servico));

        assertThatThrownBy(() -> service.criar(10L, 20L, passado, null))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("passado");

        verify(agendamentoRepository, never()).save(any());
        verify(notificacaoService, never()).notificarCriacao(any());
    }

    @Test
    @DisplayName("recusa quando o cliente não é do profissional")
    void recusaClienteInexistente() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        when(clienteRepository.findByIdAndProfissionalId(10L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(10L, 20L, inicio, null))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Cliente não encontrado");
    }

    @Test
    @DisplayName("recusa quando há conflito de horário")
    void recusaConflitoDeHorario() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        Agendamento existente = new Agendamento();
        existente.setInicio(inicio);
        existente.setFim(inicio.plusMinutes(60));

        when(clienteRepository.findByIdAndProfissionalId(10L, 1L)).thenReturn(Optional.of(cliente));
        when(servicoRepository.findByIdAndProfissionalId(20L, 1L)).thenReturn(Optional.of(servico));
        when(agendamentoRepository.encontrarConflitos(eq(1L), any(), any(), any()))
                .thenReturn(List.of(existente));

        assertThatThrownBy(() -> service.criar(10L, 20L, inicio, null))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Conflito de horário");

        verify(agendamentoRepository, never()).save(any());
    }

    @Test
    @DisplayName("propaga erro quando o horário está fora do expediente")
    void recusaForaDoExpediente() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(1).withHour(23).withMinute(0);
        when(clienteRepository.findByIdAndProfissionalId(10L, 1L)).thenReturn(Optional.of(cliente));
        when(servicoRepository.findByIdAndProfissionalId(20L, 1L)).thenReturn(Optional.of(servico));
        // Simula o DisponibilidadeService recusando por estar fora do expediente.
        org.mockito.Mockito.doThrow(new RegraNegocioException("fora do expediente"))
                .when(disponibilidadeService).validarDentroDoExpediente(anyLong(), any(), any());

        assertThatThrownBy(() -> service.criar(10L, 20L, inicio, null))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("expediente");

        verify(agendamentoRepository, never()).save(any());
    }

    // ----- alterarStatus() -----

    @Test
    @DisplayName("PENDENTE → CONFIRMADO é válido e notifica confirmação")
    void confirmaPendente() {
        Agendamento ag = agendamentoComStatus(StatusAgendamento.PENDENTE);
        when(agendamentoRepository.findByIdAndProfissionalId(5L, 1L)).thenReturn(Optional.of(ag));
        when(agendamentoRepository.save(any(Agendamento.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Agendamento> resultado = service.alterarStatus(5L, StatusAgendamento.CONFIRMADO);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getStatus()).isEqualTo(StatusAgendamento.CONFIRMADO);
        verify(notificacaoService).notificarConfirmacao(any());
    }

    @Test
    @DisplayName("PENDENTE → CONCLUIDO é inválido")
    void naoConcluiDiretoDePendente() {
        Agendamento ag = agendamentoComStatus(StatusAgendamento.PENDENTE);
        when(agendamentoRepository.findByIdAndProfissionalId(5L, 1L)).thenReturn(Optional.of(ag));

        assertThatThrownBy(() -> service.alterarStatus(5L, StatusAgendamento.CONCLUIDO))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Transição de status inválida");

        verify(agendamentoRepository, never()).save(any());
    }

    @Test
    @DisplayName("não altera um agendamento já CANCELADO (estado final)")
    void naoAlteraCancelado() {
        Agendamento ag = agendamentoComStatus(StatusAgendamento.CANCELADO);
        when(agendamentoRepository.findByIdAndProfissionalId(5L, 1L)).thenReturn(Optional.of(ag));

        assertThatThrownBy(() -> service.alterarStatus(5L, StatusAgendamento.CONFIRMADO))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("já está CANCELADO");
    }

    @Test
    @DisplayName("CONFIRMADO → CANCELADO é válido e notifica cancelamento")
    void cancelaConfirmado() {
        Agendamento ag = agendamentoComStatus(StatusAgendamento.CONFIRMADO);
        when(agendamentoRepository.findByIdAndProfissionalId(5L, 1L)).thenReturn(Optional.of(ag));
        when(agendamentoRepository.save(any(Agendamento.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Agendamento> resultado = service.alterarStatus(5L, StatusAgendamento.CANCELADO);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getStatus()).isEqualTo(StatusAgendamento.CANCELADO);
        verify(notificacaoService).notificarCancelamento(any());
    }

    // ----- reagendar() -----

    @Test
    @DisplayName("não reagenda um agendamento CONCLUIDO")
    void naoReagendaConcluido() {
        Agendamento ag = agendamentoComStatus(StatusAgendamento.CONCLUIDO);
        when(agendamentoRepository.findByIdAndProfissionalId(5L, 1L)).thenReturn(Optional.of(ag));

        assertThatThrownBy(() -> service.reagendar(5L, LocalDateTime.now().plusDays(2)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("CONCLUIDO");
    }

    // Helper: cria um agendamento com o status desejado, ligado ao dono e serviço padrão.
    private Agendamento agendamentoComStatus(StatusAgendamento status) {
        Agendamento ag = new Agendamento();
        ag.setId(5L);
        ag.setProfissional(dono);
        ag.setCliente(cliente);
        ag.setServico(servico);
        LocalDateTime inicio = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        ag.setInicio(inicio);
        ag.setFim(inicio.plusMinutes(60));
        ag.setStatus(status);
        return ag;
    }
}
