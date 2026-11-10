package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.ItemPedido;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class DashboardService {
    private final PedidoRepository pedidoRepository;

    public DashboardService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    public Map<String, Object> consultar(LocalDate inicio, LocalDate fim, String status, String agrupamento) {
        if (inicio != null && fim != null && inicio.isAfter(fim)) {
            throw new IllegalArgumentException("A data inicial não pode ser maior que a final.");
        }
        if (!"dia".equals(agrupamento) && !"semana".equals(agrupamento) && !"mes".equals(agrupamento)) {
            throw new IllegalArgumentException("O agrupamento deve ser dia, semana ou mes.");
        }
        List<Pedido> pedidos = pedidoRepository.findAll().stream()
                .filter(p -> p.getDataEntrada() != null)
                .filter(p -> inicio == null || !p.getDataEntrada().toLocalDate().isBefore(inicio))
                .filter(p -> fim == null || !p.getDataEntrada().toLocalDate().isAfter(fim))
                .filter(p -> status == null || status.isBlank() || status.equalsIgnoreCase(p.getStatus()))
                .sorted(Comparator.comparing(Pedido::getDataEntrada).reversed())
                .toList();

        Map<String, Long> situacoes = new TreeMap<>();
        Map<String, BigDecimal> vendas = new TreeMap<>();
        Map<String, Long> quantidadePorPeriodo = new TreeMap<>();
        Map<String, ProdutoResumo> produtos = new LinkedHashMap<>();
        BigDecimal faturamento = BigDecimal.ZERO;
        long pedidosValidos = 0;
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM");

        for (Pedido pedido : pedidos) {
            String situacao = pedido.getStatus() == null ? "SEM_STATUS" : pedido.getStatus();
            situacoes.merge(situacao, 1L, Long::sum);
            boolean valido = !"CANCELADO".equals(situacao) && !"CANCELAMENTO_SOLICITADO".equals(situacao);
            String periodo;
            if ("mes".equals(agrupamento)) {
                periodo = pedido.getDataEntrada().format(formato);
            } else if ("semana".equals(agrupamento)) {
                LocalDate data = pedido.getDataEntrada().toLocalDate();
                WeekFields semana = WeekFields.ISO;
                periodo = String.format("%04d-S%02d", data.get(semana.weekBasedYear()), data.get(semana.weekOfWeekBasedYear()));
            } else {
                periodo = pedido.getDataEntrada().toLocalDate().toString();
            }
            quantidadePorPeriodo.merge(periodo, 1L, Long::sum);
            if (valido) {
                BigDecimal total = pedido.getValorTotal() == null ? BigDecimal.ZERO : pedido.getValorTotal();
                faturamento = faturamento.add(total);
                pedidosValidos++;
                vendas.merge(periodo, total, BigDecimal::add);
                if (pedido.getItens() != null) {
                    for (ItemPedido item : pedido.getItens()) {
                        if (item == null || item.getQuantidade() <= 0) continue;
                        String chave = item.getProdutoId() == null ? "nome:" + item.getNome() : item.getProdutoId();
                        ProdutoResumo produto = produtos.computeIfAbsent(chave,
                                k -> new ProdutoResumo(item.getNome() == null ? "Produto sem nome" : item.getNome()));
                        produto.quantidade += item.getQuantidade();
                        BigDecimal subtotal = item.getSubtotal() != null ? item.getSubtotal()
                                : (item.getPrecoUnitario() == null ? BigDecimal.ZERO
                                : item.getPrecoUnitario().multiply(BigDecimal.valueOf(item.getQuantidade())));
                        produto.total = produto.total.add(subtotal);
                    }
                }
            } else {
                vendas.putIfAbsent(periodo, BigDecimal.ZERO);
            }
        }

        List<Map<String, Object>> serie = new ArrayList<>();
        for (String periodo : quantidadePorPeriodo.keySet()) {
            serie.add(Map.of("periodo", periodo, "vendas", vendas.getOrDefault(periodo, BigDecimal.ZERO),
                    "pedidos", quantidadePorPeriodo.get(periodo)));
        }
        List<Map<String, Object>> ranking = produtos.values().stream()
                .sorted(Comparator.comparingInt((ProdutoResumo p) -> p.quantidade).reversed()
                        .thenComparing(p -> p.nome))
                .limit(10)
                .map(p -> Map.<String, Object>of("nome", p.nome, "quantidade", p.quantidade, "total", p.total))
                .toList();
        List<Map<String, Object>> recentes = pedidos.stream().limit(10).map(p -> Map.<String, Object>of(
                "id", p.getId() == null ? "" : p.getId(),
                "data", p.getDataEntrada().toString(),
                "status", p.getStatus() == null ? "SEM_STATUS" : p.getStatus(),
                "total", p.getValorTotal() == null ? BigDecimal.ZERO : p.getValorTotal())).toList();

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("totalPedidos", pedidos.size());
        resultado.put("pedidosValidos", pedidosValidos);
        resultado.put("faturamento", faturamento);
        resultado.put("ticketMedio", pedidosValidos == 0 ? BigDecimal.ZERO
                : faturamento.divide(BigDecimal.valueOf(pedidosValidos), 2, RoundingMode.HALF_UP));
        resultado.put("cancelados", situacoes.getOrDefault("CANCELADO", 0L));
        resultado.put("situacoes", situacoes);
        resultado.put("vendasPorPeriodo", serie);
        resultado.put("produtosMaisVendidos", ranking);
        resultado.put("pedidosRecentes", recentes);
        return resultado;
    }

    private static class ProdutoResumo {
        final String nome;
        int quantidade;
        BigDecimal total = BigDecimal.ZERO;

        ProdutoResumo(String nome) {
            this.nome = nome;
        }
    }
}