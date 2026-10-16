package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Entregador;
import com.umc.pediupatrao.repository.EntregadorRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EntregadorService {
    private final EntregadorRepository repository;

    public EntregadorService(EntregadorRepository repository) {
        this.repository = repository;
    }

    public List<Entregador> listarTodos() {
        return repository.findAll();
    }

    public Entregador cadastrar(String nome) {
        if (nome == null || nome.isBlank() || nome.trim().length() > 100) {
            throw new IllegalArgumentException("Informe um nome de até 100 caracteres.");
        }
        Entregador entregador = new Entregador();
        entregador.setNome(nome.trim());
        return repository.save(entregador);
    }
}