package com.agendamento.api.service;

import com.agendamento.api.model.Profissional;
import com.agendamento.api.model.Servico;
import com.agendamento.api.repository.ServicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// Todas as operações são feitas no escopo do profissional autenticado:
// o catálogo de serviços de cada freelancer é isolado.
@Service
public class ServicoService {

    private final ServicoRepository servicoRepository;
    private final ProfissionalAtualService profissionalAtual;

    public ServicoService(ServicoRepository servicoRepository,
            ProfissionalAtualService profissionalAtual) {
        this.servicoRepository = servicoRepository;
        this.profissionalAtual = profissionalAtual;
    }

    public Servico criar(Servico servico) {
        Profissional dono = profissionalAtual.obter();
        servico.setProfissional(dono);
        return servicoRepository.save(servico);
    }

    public List<Servico> listarTodos() {
        return servicoRepository.findByProfissionalId(profissionalAtual.obter().getId());
    }

    public Optional<Servico> buscarPorId(Long id) {
        return servicoRepository.findByIdAndProfissionalId(id, profissionalAtual.obter().getId());
    }

    // Atualiza os dados do serviço mantendo o mesmo ID e o mesmo dono.
    public Optional<Servico> atualizar(Long id, Servico dados) {
        return servicoRepository.findByIdAndProfissionalId(id, profissionalAtual.obter().getId())
                .map(existente -> {
                    existente.setNome(dados.getNome());
                    existente.setDescricao(dados.getDescricao());
                    existente.setDuracaoMinutos(dados.getDuracaoMinutos());
                    existente.setPreco(dados.getPreco());
                    return servicoRepository.save(existente);
                });
    }

    public boolean deletar(Long id) {
        Optional<Servico> servico = servicoRepository.findByIdAndProfissionalId(
                id, profissionalAtual.obter().getId());
        if (servico.isPresent()) {
            servicoRepository.delete(servico.get());
            return true;
        }
        return false;
    }
}
