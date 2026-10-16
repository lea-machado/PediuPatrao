package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @PostMapping
    public Pedido criarPedido(@RequestBody Pedido pedido, Authentication authentication) {
        return pedidoService.criarPedido(pedido, authentication.getName());
    }

    @GetMapping
    public List<Pedido> listarPedidos() {
        return pedidoService.listarPedidos();
    }

    @PutMapping("/{id}/solicitar-cancelamento")
    public Pedido solicitarCancelamento(@PathVariable String id, @RequestBody Map<String, String> dados,
            Authentication authentication) {
        return pedidoService.solicitarCancelamento(id, dados.get("justificativa"), authentication.getName());
    }

    @PostMapping("/{id}/aprovar-cancelamento")
    public Pedido aprovarCancelamento(@PathVariable String id, @RequestBody Map<String, String> dados) {
        return pedidoService.aprovarCancelamento(id, dados.get("username"), dados.get("password"));
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelarPedido(@PathVariable String id) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @PutMapping("/{id}/status")
    public Pedido atualizarStatus(@PathVariable String id, @RequestParam String status,
            Authentication authentication) {
        return pedidoService.atualizarStatus(id, status, authentication.getName());
    }

    @PutMapping("/{id}/solicitar-desconto")
    public Pedido solicitarDesconto(@PathVariable String id, @RequestBody Map<String, BigDecimal> dados,
            Authentication authentication) {
        return pedidoService.solicitarDesconto(id, dados.get("desconto"), authentication.getName());
    }

    @PostMapping("/{id}/aprovar-desconto")
    public Pedido aprovarDesconto(@PathVariable String id, @RequestBody Map<String, String> dados) {
        return pedidoService.aprovarDesconto(id, dados.get("username"), dados.get("password"));
    }

    @PutMapping("/{id}/desconto")
    public ResponseEntity<Void> descontoDiretoBloqueado(@PathVariable String id) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @PutMapping("/{id}/entregador")
    public Pedido atribuirEntregador(@PathVariable String id, @RequestBody Map<String, String> dados) {
        return pedidoService.atribuirEntregador(id, dados.get("entregadorId"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> dadosInvalidos(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> estadoInvalido(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("erro", ex.getMessage()));
    }
}
