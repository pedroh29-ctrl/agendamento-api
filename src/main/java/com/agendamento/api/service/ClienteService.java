package com.agendamento.api.service;

import com.agendamento.api.exception.RegraNegocioException;
import com.agendamento.api.model.Cliente;
import com.agendamento.api.model.Profissional;
import com.agendamento.api.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// Todas as operações são feitas no escopo do profissional autenticado:
// um profissional nunca enxerga nem altera clientes de outro.
@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ProfissionalAtualService profissionalAtual;

    public ClienteService(ClienteRepository clienteRepository,
            ProfissionalAtualService profissionalAtual) {
        this.clienteRepository = clienteRepository;
        this.profissionalAtual = profissionalAtual;
    }

    // Cadastra um cliente na carteira do profissional logado, impedindo
    // e-mail duplicado dentro dessa mesma carteira.
    public Cliente criar(Cliente cliente) {
        Profissional dono = profissionalAtual.obter();

        if (clienteRepository.existsByEmailAndProfissionalId(cliente.getEmail(), dono.getId())) {
            throw new RegraNegocioException("Você já tem um cliente com o e-mail " + cliente.getEmail());
        }

        cliente.setProfissional(dono);
        return clienteRepository.save(cliente);
    }

    public List<Cliente> listarTodos() {
        return clienteRepository.findByProfissionalId(profissionalAtual.obter().getId());
    }

    public Optional<Cliente> buscarPorId(Long id) {
        return clienteRepository.findByIdAndProfissionalId(id, profissionalAtual.obter().getId());
    }

    // Atualiza os dados do cliente. Se o e-mail mudou, verifica se o novo
    // e-mail já não pertence a outro cliente do mesmo profissional.
    public Optional<Cliente> atualizar(Long id, Cliente dados) {
        Profissional dono = profissionalAtual.obter();
        return clienteRepository.findByIdAndProfissionalId(id, dono.getId()).map(existente -> {
            if (!existente.getEmail().equals(dados.getEmail())
                    && clienteRepository.existsByEmailAndProfissionalId(dados.getEmail(), dono.getId())) {
                throw new RegraNegocioException("Você já tem um cliente com o e-mail " + dados.getEmail());
            }
            existente.setNome(dados.getNome());
            existente.setEmail(dados.getEmail());
            existente.setTelefone(dados.getTelefone());
            return clienteRepository.save(existente);
        });
    }

    public boolean deletar(Long id) {
        Optional<Cliente> cliente = clienteRepository.findByIdAndProfissionalId(
                id, profissionalAtual.obter().getId());
        if (cliente.isPresent()) {
            clienteRepository.delete(cliente.get());
            return true;
        }
        return false;
    }
}
