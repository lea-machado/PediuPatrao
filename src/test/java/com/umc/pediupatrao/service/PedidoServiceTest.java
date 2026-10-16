package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.ItemPedido;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock private PedidoRepository pedidoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ProdutoRepository produtoRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private EntregadorRepository entregadorRepository;
    @Mock private AuditoriaService auditoriaService;
    @InjectMocks private PedidoService pedidoService;

    private Pedido pedidoComStatus(String status) {
        Pedido pedido = new Pedido();
        pedido.setId("pedido1");
        pedido.setStatus(status);
        pedido.setSubtotal(new BigDecimal("100.00"));
        pedido.setValorTotal(new BigDecimal("100.00"));
        return pedido;
    }

    @Test
    void deveCalcularTotalUsandoPrecoDoBancoEmVezDoValorEnviado() {
        Produto produto = new Produto();
        produto.setId("produto1");
        produto.setNome("Pizza");
        produto.setPreco(35.0);
        produto.setAtivo(true);
        ItemPedido item = new ItemPedido();
        item.setProdutoId("produto1");
        item.setQuantidade(2);
        item.setPrecoUnitario(new BigDecimal("0.01")); // Valor malicioso enviado pelo cliente.
        Pedido enviado = new Pedido();
        enviado.setClienteId("cliente1");
        enviado.setItens(List.of(item));
        enviado.setValorTotal(BigDecimal.ONE); // Deve ser ignorado.
        when(clienteRepository.existsById("cliente1")).thenReturn(true);
        when(produtoRepository.findById("produto1")).thenReturn(Optional.of(produto));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));

        Pedido criado = pedidoService.criarPedido(enviado, "atendente01");

        assertEquals(0, criado.getValorTotal().compareTo(new BigDecimal("70.00")));
        assertEquals("RECEBIDO", criado.getStatus());
        assertEquals("atendente01", criado.getRegistradoPor());
        assertNotNull(criado.getDataEntrada());
        verify(auditoriaService).registrar(eq("CRIACAO"), eq("PEDIDO"), any(), anyMap(), anyMap(), isNull());
    }

    @Test
    void deveRejeitarCriacaoParaClienteInexistente() {
        Pedido enviado = new Pedido();
        enviado.setClienteId("inexistente");
        when(clienteRepository.existsById("inexistente")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> pedidoService.criarPedido(enviado, "atendente01"));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void deveBloquearTransicaoDiretaDeRecebidoParaFinalizado() {
        when(pedidoRepository.findById("pedido1")).thenReturn(Optional.of(pedidoComStatus("RECEBIDO")));
        assertThrows(IllegalStateException.class,
                () -> pedidoService.atualizarStatus("pedido1", "FINALIZADO", "gerente01"));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void devePermitirTransicaoDeRecebidoParaPreparacao() {
        when(pedidoRepository.findById("pedido1")).thenReturn(Optional.of(pedidoComStatus("RECEBIDO")));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));
        Pedido atualizado = pedidoService.atualizarStatus("pedido1", "EM_PREPARACAO", "gerente01");
        assertEquals("EM_PREPARACAO", atualizado.getStatus());
        verify(auditoriaService).registrar(eq("STATUS"), eq("PEDIDO"), eq("pedido1"), anyMap(), anyMap(), isNull());
    }

    @Test
    void deveBloquearDescontoAcimaDeVintePorCento() {
        when(pedidoRepository.findById("pedido1")).thenReturn(Optional.of(pedidoComStatus("RECEBIDO")));
        assertThrows(IllegalArgumentException.class,
                () -> pedidoService.solicitarDesconto("pedido1", new BigDecimal("21"), "atendente01"));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void deveAplicarDescontoSomenteComCredenciaisValidasDoGerente() {
        Usuario gerente = new Usuario();
        gerente.setUsername("gerente01");
        gerente.setRole("gerente");
        gerente.setPassword("hash-bcrypt");
        Pedido pedido = pedidoComStatus("RECEBIDO");
        pedido.setDescontoSolicitado(new BigDecimal("10"));
        when(usuarioRepository.findByUsername("gerente01")).thenReturn(Optional.of(gerente));
        when(passwordEncoder.matches("senha-correta", "hash-bcrypt")).thenReturn(true);
        when(pedidoRepository.findById("pedido1")).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));

        Pedido atualizado = pedidoService.aprovarDesconto("pedido1", "gerente01", "senha-correta");

        assertEquals(0, atualizado.getValorTotal().compareTo(new BigDecimal("90.00")));
        assertNull(atualizado.getDescontoSolicitado());
        verify(auditoriaService).registrarGerente(eq("DESCONTO"), eq("PEDIDO"), eq("pedido1"),
                eq("gerente01"), anyMap(), anyMap(), isNull());
    }

    @Test
    void deveRejeitarAprovacaoComSenhaErrada() {
        Usuario gerente = new Usuario();
        gerente.setUsername("gerente01");
        gerente.setRole("gerente");
        gerente.setPassword("hash-bcrypt");
        when(usuarioRepository.findByUsername("gerente01")).thenReturn(Optional.of(gerente));
        when(passwordEncoder.matches("senha-errada", "hash-bcrypt")).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> pedidoService.aprovarCancelamento("pedido1", "gerente01", "senha-errada"));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void deveCancelarPedidoSolicitadoComAprovacaoDoGerente() {
        Usuario gerente = new Usuario();
        gerente.setUsername("gerente01");
        gerente.setRole("gerente");
        gerente.setPassword("hash-bcrypt");
        Pedido pedido = pedidoComStatus("CANCELAMENTO_SOLICITADO");
        pedido.setStatusAntesCancelamento("RECEBIDO");
        pedido.setJustificativaCancelamento("Pedido duplicado");
        when(usuarioRepository.findByUsername("gerente01")).thenReturn(Optional.of(gerente));
        when(passwordEncoder.matches("senha-correta", "hash-bcrypt")).thenReturn(true);
        when(pedidoRepository.findById("pedido1")).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));

        Pedido cancelado = pedidoService.aprovarCancelamento("pedido1", "gerente01", "senha-correta");

        assertEquals("CANCELADO", cancelado.getStatus());
        assertNotNull(cancelado.getDataCancelamento());
        verify(auditoriaService).registrarGerente(eq("CANCELAMENTO"), eq("PEDIDO"), eq("pedido1"),
                eq("gerente01"), anyMap(), anyMap(), eq("Pedido duplicado"));
    }
}
