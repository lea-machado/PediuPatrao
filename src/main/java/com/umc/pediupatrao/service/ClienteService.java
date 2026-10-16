package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Cliente;
import com.umc.pediupatrao.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Objects;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private AuditoriaService auditoriaService;

    public Cliente novoCliente(Cliente cliente) {
        return salvar(cliente);
    }

    public List<Cliente> listarClientes() {
        return clienteRepository.findAll();
    }

    // Método para excluir cliente
    @Transactional
    public void excluir(String id) {
        Cliente existente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado."));
        clienteRepository.deleteById(id);
        auditoriaService.registrar("EXCLUSAO", "CLIENTE", id,
                Map.of("nome", existente.getNome() == null ? "" : existente.getNome()), Map.of(), null);
    }

    public Optional<Cliente> buscarPorId(String id) {
        return clienteRepository.findById(id);
    }

    // Método para salvar um novo cliente ou atualizar um cliente existente
    @Transactional
    public Cliente salvar(Cliente cliente) {
        boolean novo = cliente.getId() == null || cliente.getId().isBlank();
        Cliente antigo = novo ? null
                : clienteRepository.findById(cliente.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado para atualização."));
        Map<String, Object> anterior = new LinkedHashMap<>();
        Map<String, Object> depois = new LinkedHashMap<>();
        if (novo) {
            comparar("nome", null, cliente.getNome(), anterior, depois);
            comparar("telefone", null, cliente.getTelefone(), anterior, depois);
            comparar("cep", null, cliente.getCep(), anterior, depois);
            comparar("logradouro", null, cliente.getLogradouro(), anterior, depois);
            comparar("numero", null, cliente.getNumero(), anterior, depois);
            comparar("complemento", null, cliente.getComplemento(), anterior, depois);
            comparar("bairro", null, cliente.getBairro(), anterior, depois);
            comparar("cidade", null, cliente.getCidade(), anterior, depois);
            comparar("estado", null, cliente.getEstado(), anterior, depois);
            comparar("endereco", null, cliente.getEndereco(), anterior, depois);
        } else {
            comparar("nome", antigo.getNome(), cliente.getNome(), anterior, depois);
            comparar("telefone", antigo.getTelefone(), cliente.getTelefone(), anterior, depois);
            comparar("cep", antigo.getCep(), cliente.getCep(), anterior, depois);
            comparar("logradouro", antigo.getLogradouro(), cliente.getLogradouro(), anterior, depois);
            comparar("numero", antigo.getNumero(), cliente.getNumero(), anterior, depois);
            comparar("complemento", antigo.getComplemento(), cliente.getComplemento(), anterior, depois);
            comparar("bairro", antigo.getBairro(), cliente.getBairro(), anterior, depois);
            comparar("cidade", antigo.getCidade(), cliente.getCidade(), anterior, depois);
            comparar("estado", antigo.getEstado(), cliente.getEstado(), anterior, depois);
            comparar("endereco", antigo.getEndereco(), cliente.getEndereco(), anterior, depois);
        }
        Cliente salvo = clienteRepository.save(cliente);
        if (novo || !depois.isEmpty())
            auditoriaService.registrar(novo ? "CRIACAO" : "ALTERACAO", "CLIENTE",
                    salvo.getId(), anterior, depois, null);
        return salvo;
    }

    private void comparar(String campo, String antigo, String novo,
            Map<String, Object> anterior, Map<String, Object> depois) {
        if (Objects.equals(antigo, novo))
            return;
        anterior.put(campo, antigo == null ? "" : antigo);
        depois.put(campo, novo == null ? "" : novo);
    }
}
