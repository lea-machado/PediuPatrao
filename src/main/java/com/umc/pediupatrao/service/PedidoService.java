package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.repository.PedidoRepository;
import com.umc.pediupatrao.repository.UsuarioRepository;
import com.umc.pediupatrao.repository.ProdutoRepository;
import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.entity.ItemPedido;
import com.umc.pediupatrao.entity.Produto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.access.AccessDeniedException;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import com.umc.pediupatrao.repository.ClienteRepository;
import com.umc.pediupatrao.repository.EntregadorRepository;
import com.umc.pediupatrao.entity.Entregador;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoService {
    @Autowired
    private PedidoRepository pedidoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ProdutoRepository produtoRepository;
    @Autowired
    private AuditoriaService auditoriaService;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private EntregadorRepository entregadorRepository;

    @Transactional 
    public Pedido criarPedido(Pedido pedido, String username) {
        if (pedido.getClienteId() == null || pedido.getClienteId().isBlank())
            throw new IllegalArgumentException("Informe o cliente.");
        if (!clienteRepository.existsById(pedido.getClienteId())) {
            throw new IllegalArgumentException("Cliente não encontrado.");
        }
        if (pedido.getItens() == null || pedido.getItens().isEmpty())
            throw new IllegalArgumentException("Informe pelo menos um item.");
        List<ItemPedido> itens = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        for (ItemPedido enviado : pedido.getItens()) {
            if (enviado.getProdutoId() == null || enviado.getQuantidade() <= 0)
                throw new IllegalArgumentException("Produto e quantidade devem ser válidos.");
            Produto produto = produtoRepository.findById(enviado.getProdutoId())
                    .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado."));
            if (Boolean.FALSE.equals(produto.getAtivo()) || produto.getPreco() == null ||
                    !Double.isFinite(produto.getPreco()) || produto.getPreco() < 0)
                throw new IllegalArgumentException("Produto indisponível ou sem preço válido.");
            ItemPedido item = new ItemPedido();
            item.setProdutoId(produto.getId());
            item.setNome(produto.getNome());
            item.setQuantidade(enviado.getQuantidade());
            item.setPrecoUnitario(BigDecimal.valueOf(produto.getPreco()).setScale(2, RoundingMode.HALF_UP));
            item.setSubtotal(item.getPrecoUnitario().multiply(BigDecimal.valueOf(item.getQuantidade())));
            subtotal = subtotal.add(item.getSubtotal());
            itens.add(item);
        }
        Pedido novo = new Pedido();
        novo.setClienteId(pedido.getClienteId());
        novo.setItens(itens);
        novo.setSubtotal(subtotal);
        novo.setDesconto(BigDecimal.ZERO);
        novo.setValorTotal(subtotal);
        novo.setStatus("RECEBIDO");
        novo.setRegistradoPor(username);
        novo.setDataEntrada(LocalDateTime.now());
        Pedido salvo = pedidoRepository.save(novo);
        auditoriaService.registrar("CRIACAO", "PEDIDO", salvo.getId(), java.util.Map.of(),
                java.util.Map.of("status", salvo.getStatus(), "subtotal", subtotal,
                        "valorTotal", subtotal, "itens", itens, "registradoPor", username),
                null);
        return salvo;
    }

    public List<Pedido> listarPedidos() {
        return pedidoRepository.findAll();
    }

    @Transactional 
    public Pedido solicitarDesconto(String id, BigDecimal percentual, String username) {
        Pedido pedido = buscarPedido(id);
        validarDesconto(pedido, percentual);
        if (pedido.getDescontoSolicitado() != null)
            throw new IllegalStateException("Já existe uma solicitação de desconto pendente.");
        pedido.setDescontoSolicitado(percentual);
        pedido.setDescontoSolicitadoPor(username);
        pedido.setDataSolicitacaoDesconto(LocalDateTime.now());
        Pedido salvo = pedidoRepository.save(pedido);
        auditoriaService.registrar("SOLICITACAO_DESCONTO", "PEDIDO", id,
                java.util.Map.of("descontoSolicitado", "nenhum"),
                java.util.Map.of("percentualSolicitado", percentual, "solicitadoPor", username), null);
        return salvo;
    }

    @Transactional 
    public Pedido aprovarDesconto(String id, String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank())
            throw new IllegalArgumentException("Informe usuário e senha do gerente.");
        Usuario gerente = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new AccessDeniedException("Credenciais do gerente inválidas."));
        if (!"gerente".equalsIgnoreCase(gerente.getRole()) ||
                !passwordEncoder.matches(password, gerente.getPassword()))
            throw new AccessDeniedException("Credenciais do gerente inválidas.");

        Pedido pedido = buscarPedido(id);
        BigDecimal percentual = pedido.getDescontoSolicitado();
        if (percentual == null)
            throw new IllegalStateException("Não existe desconto aguardando aprovação.");
        validarDesconto(pedido, percentual);
        BigDecimal anteriorPercentual = pedido.getPercentualDesconto() == null ? BigDecimal.ZERO
                : pedido.getPercentualDesconto();
        BigDecimal anteriorDesconto = pedido.getDesconto() == null ? BigDecimal.ZERO : pedido.getDesconto();
        BigDecimal anteriorTotal = pedido.getValorTotal();
        BigDecimal valor = pedido.getSubtotal().multiply(percentual)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        pedido.setPercentualDesconto(percentual);
        pedido.setDesconto(valor);
        pedido.setValorTotal(pedido.getSubtotal().subtract(valor));
        pedido.setDescontoSolicitado(null);
        pedido.setDescontoAprovadoPor(username);
        pedido.setDataAprovacaoDesconto(LocalDateTime.now());
        Pedido salvo = pedidoRepository.save(pedido);
        auditoriaService.registrarGerente("DESCONTO", "PEDIDO", id, username,
                java.util.Map.of("percentual", anteriorPercentual, "desconto", anteriorDesconto, "total",
                        anteriorTotal),
                java.util.Map.of("percentual", percentual, "desconto", valor, "total", salvo.getValorTotal(),
                        "aprovadoPor", username),
                null);
        return salvo;
    }

    private void validarDesconto(Pedido pedido, BigDecimal percentual) {
        if (!Set.of("RECEBIDO", "EM_PREPARACAO", "PRONTO").contains(pedido.getStatus()) ||
                pedido.getDataSaida() != null)
            throw new IllegalStateException("Desconto não permitido neste status.");
        if (percentual == null || percentual.compareTo(BigDecimal.ZERO) <= 0 ||
                percentual.compareTo(new BigDecimal("20")) > 0 || percentual.scale() > 2)
            throw new IllegalArgumentException("O desconto deve ser maior que zero e no máximo 20%.");
    }

    @Transactional
    public Pedido atualizarStatus(String id, String novoStatus, String username) {
        Pedido pedido = buscarPedido(id);
        if (pedido.getDescontoSolicitado() != null)
            throw new IllegalStateException("Aguarde a aprovação do desconto antes de avançar o pedido.");
        String atual = pedido.getStatus();
        boolean permitido = ("RECEBIDO".equals(atual) && "EM_PREPARACAO".equals(novoStatus)) ||
                ("EM_PREPARACAO".equals(atual) && "PRONTO".equals(novoStatus)) ||
                ("PRONTO".equals(atual) && Set.of("SAIU_PARA_ENTREGA", "RETIRADO").contains(novoStatus)) ||
                (Set.of("SAIU_PARA_ENTREGA", "RETIRADO").contains(atual) && "FINALIZADO".equals(novoStatus));
        if (!permitido)
            throw new IllegalStateException("Transição de status não permitida: " + atual + " -> " + novoStatus);
        if ("SAIU_PARA_ENTREGA".equals(novoStatus) && (pedido.getEntregadorId() == null ||
                !entregadorRepository.existsById(pedido.getEntregadorId()))) {
            throw new IllegalStateException("Atribua um entregador antes da saída para entrega.");
        }
        Map<String, Object> anterior = new LinkedHashMap<>();
        anterior.put("status", atual);
        if (Set.of("SAIU_PARA_ENTREGA", "RETIRADO").contains(novoStatus)) {
            anterior.put("dataSaida", pedido.getDataSaida());
            anterior.put("responsavelSaida", pedido.getResponsavelSaida());
        }
        if (Set.of("SAIU_PARA_ENTREGA", "RETIRADO").contains(novoStatus)) {
            pedido.setDataSaida(LocalDateTime.now());
            pedido.setResponsavelSaida(username);
        }
        pedido.setStatus(novoStatus);
        Pedido salvo = pedidoRepository.save(pedido);
        java.util.Map<String, Object> depois = new java.util.LinkedHashMap<>();
        depois.put("status", novoStatus);
        if (salvo.getDataSaida() != null && Set.of("SAIU_PARA_ENTREGA", "RETIRADO").contains(novoStatus)) {
            depois.put("dataSaida", salvo.getDataSaida());
            depois.put("responsavelSaida", salvo.getResponsavelSaida());
        }
        auditoriaService.registrar("STATUS", "PEDIDO", id,
                anterior, depois, null);
        return salvo;
    }

    @Transactional 
    public Pedido solicitarCancelamento(String id, String justificativa, String username) {
        Pedido pedido = buscarPedido(id);
        if (!Set.of("RECEBIDO", "EM_PREPARACAO", "PRONTO").contains(pedido.getStatus()))
            throw new IllegalStateException("Pedido já saiu ou não permite cancelamento.");
        if (justificativa == null || justificativa.isBlank())
            throw new IllegalArgumentException("A justificativa é obrigatória.");
        if (pedido.getDescontoSolicitado() != null)
            throw new IllegalStateException("Resolva a solicitação de desconto antes de cancelar.");
        pedido.setStatusAntesCancelamento(pedido.getStatus());
        pedido.setStatus("CANCELAMENTO_SOLICITADO");
        pedido.setJustificativaCancelamento(justificativa.trim());
        pedido.setCancelamentoSolicitadoPor(username);
        Pedido salvo = pedidoRepository.save(pedido);
        auditoriaService.registrar("SOLICITACAO_CANCELAMENTO", "PEDIDO", id,
                java.util.Map.of("status", salvo.getStatusAntesCancelamento()),
                java.util.Map.of("status", salvo.getStatus(), "solicitadoPor", username), justificativa.trim());
        return salvo;
    }

    @Transactional 
    public Pedido aprovarCancelamento(String id, String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank())
            throw new IllegalArgumentException("Informe usuário e senha do gerente.");
        Usuario gerente = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new AccessDeniedException("Credenciais do gerente inválidas."));
        if (!"gerente".equalsIgnoreCase(gerente.getRole()) ||
                !passwordEncoder.matches(password, gerente.getPassword()))
            throw new AccessDeniedException("Credenciais do gerente inválidas.");
        Pedido pedido = buscarPedido(id);
        if (!"CANCELAMENTO_SOLICITADO".equals(pedido.getStatus()) ||
                !Set.of("RECEBIDO", "EM_PREPARACAO", "PRONTO").contains(pedido.getStatusAntesCancelamento()) ||
                pedido.getJustificativaCancelamento() == null || pedido.getJustificativaCancelamento().isBlank() ||
                pedido.getDataSaida() != null)
              throw new IllegalStateException("Pedido não pode ser cancelado.");
        pedido.setStatus("CANCELADO");
        pedido.setCancelamentoAprovadoPor(username);
        pedido.setDataCancelamento(LocalDateTime.now());
        Pedido salvo = pedidoRepository.save(pedido);
        auditoriaService.registrarGerente("CANCELAMENTO", "PEDIDO", id, username,
                java.util.Map.of("status", "CANCELAMENTO_SOLICITADO"),
                java.util.Map.of("status", "CANCELADO", "aprovadoPor", username,
                        "dataCancelamento", salvo.getDataCancelamento()),
                salvo.getJustificativaCancelamento());
        return salvo;
    }

    @Transactional 
    public Pedido atribuirEntregador(String pedidoId, String entregadorId) {
        Pedido pedido = buscarPedido(pedidoId);
        if (!Set.of("RECEBIDO", "EM_PREPARACAO", "PRONTO").contains(pedido.getStatus()) ||
                pedido.getDataSaida() != null) {
            throw new IllegalStateException("Não é possível atribuir entregador neste status.");
        }
        if (entregadorId == null || entregadorId.isBlank()) {
            throw new IllegalArgumentException("Selecione um entregador.");
        }
        Entregador entregador = entregadorRepository.findById(entregadorId)
                .orElseThrow(() -> new IllegalArgumentException("Entregador não encontrado."));
        String anteriorId = pedido.getEntregadorId();
        pedido.setEntregadorId(entregador.getId());
        Pedido salvo = pedidoRepository.save(pedido);
        java.util.Map<String, Object> anterior = new java.util.LinkedHashMap<>();
        anterior.put("entregadorId", anteriorId);
        auditoriaService.registrar("ATRIBUICAO_ENTREGADOR", "PEDIDO", pedidoId,
                anterior, java.util.Map.of("entregadorId", entregador.getId(),
                        "entregadorNome", entregador.getNome()),
                null);
        return salvo;
    }

    private Pedido buscarPedido(String id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado."));
    }
}