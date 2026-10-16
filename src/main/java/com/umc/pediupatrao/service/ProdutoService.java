package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    public Produto novoProduto(Produto produto) {
        return produtoRepository.save(produto);
    }

    public List<Produto> listarProdutos() {
        return produtoRepository.findAll();
    }

    // Método para excluir produto
    public void excluir(String id) {
        if (!produtoRepository.existsById(id)) {
            throw new IllegalArgumentException("Produto não encontrado.");
        }
        produtoRepository.deleteById(id);
    }

    private void validarProduto(Produto produto) {
        if (produto.getNome() == null || produto.getNome().isBlank()) {
            throw new IllegalArgumentException("Informe o nome do produto.");
        }
        if (produto.getTipo() == null || produto.getTipo().isBlank()) {
            throw new IllegalArgumentException("Informe o tipo do produto.");
        }
        if (produto.getPreco() == null || !Double.isFinite(produto.getPreco()) || produto.getPreco() < 0) {
            throw new IllegalArgumentException("Informe um preço válido maior ou igual a zero.");
        }
        produto.setNome(produto.getNome().trim());
        produto.setTipo(produto.getTipo().trim());
    }

    public Produto salvar(Produto produto) {
        validarProduto(produto);
        if (produto.getId() != null && !produto.getId().isBlank()
                && !produtoRepository.existsById(produto.getId())) {
            throw new IllegalArgumentException("Produto não encontrado para atualização.");
        }
        return produtoRepository.save(produto);
    }
}
