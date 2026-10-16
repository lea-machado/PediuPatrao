package com.umc.pediupatrao.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.umc.pediupatrao.entity.Cliente;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.service.ClienteService;
import com.umc.pediupatrao.service.PedidoService;
import com.umc.pediupatrao.service.ProdutoService;
import com.umc.pediupatrao.service.UsuarioService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import com.umc.pediupatrao.repository.UsuarioRepository;
import com.umc.pediupatrao.service.EntregadorService;
import java.util.stream.Collectors;
import com.umc.pediupatrao.service.AuditoriaService;
import com.umc.pediupatrao.entity.RegistroAuditoria;

@Controller
public class HomeController {

    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    @ModelAttribute
    public void permissoesMenu(Model model, Authentication authentication) {
        boolean gerente = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(p -> p.getAuthority().equals("ROLE_gerente"));
        boolean admin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(p -> p.getAuthority().equals("ROLE_admin"));
        model.addAttribute("podeGerenciarProdutos", gerente);
        model.addAttribute("podeGerenciarUsuarios", admin);
        model.addAttribute("podeCadastrarEntregador", admin);
        model.addAttribute("podeVerAuditoria", admin || gerente);
    }

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EntregadorService entregadorService;

    @Autowired
    private AuditoriaService auditoriaService;

    @GetMapping("/auditoria")
    public String auditoria(Model model) {
        List<RegistroAuditoria> registros = auditoriaService.listar();
        registros.sort(java.util.Comparator.comparing(RegistroAuditoria::getDataHora,
                java.util.Comparator.nullsFirst(java.util.Comparator.naturalOrder())).reversed());
        model.addAttribute("registrosAuditoria", registros);
        model.addAttribute("content", "auditoria/lista :: content");
        return "layout";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("content", "home :: content");
        return "layout";
    }

    // ========================
    // USUÁRIOS
    // ========================
    @GetMapping("/usuarios")
    public String listarUsuarios(Model model) {
        List<Usuario> usuarios = (List<Usuario>) usuarioService.listarTodos();
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("content", "usuarios/lista :: content");
        log.info("Carregando fragmento: usuarios/lista :: content");
        return "layout";
    }

    @GetMapping("/usuarios/novo")
    public String novoUsuarioForm(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("content", "usuarios/form :: content");
        return "layout";
    }

    @GetMapping("/usuarios/editar/{id}")
    public String editarUsuarioForm(@PathVariable String id, Model model,
            RedirectAttributes redirectAttributes) {
        boolean encontrado = usuarioService.buscarPorId(id).map(u -> {
            u.setPassword("");
            model.addAttribute("usuario", u);
            return true;
        }).orElse(false);

        if (!encontrado) {
            redirectAttributes.addFlashAttribute("erro", "Usuário não encontrado.");
            return "redirect:/usuarios";
        }

        model.addAttribute("content", "usuarios/form :: content");
        return "layout";
    }

    @PostMapping("/usuarios/salvar")
    public String salvarUsuario(@ModelAttribute Usuario usuario,
            RedirectAttributes redirectAttributes) {
        usuarioService.salvarUsuario(usuario);
        redirectAttributes.addFlashAttribute("sucesso", "Usuário criado com sucesso!");
        return "redirect:/usuarios";
    }

    @PostMapping("/usuarios/editar/{id}")
    public String atualizarUsuario(@PathVariable String id,
            @ModelAttribute Usuario usuario,
            RedirectAttributes redirectAttributes) {
        usuarioService.atualizarUsuario(id, usuario);
        redirectAttributes.addFlashAttribute("sucesso", "Usuário atualizado com sucesso!");
        return "redirect:/usuarios";
    }

    @PostMapping("/usuarios/deletar/{id}")
    public String deletarUsuario(@PathVariable String id,
            RedirectAttributes redirectAttributes) {
        usuarioService.deletarUsuario(id);
        redirectAttributes.addFlashAttribute("sucesso", "Usuário removido com sucesso!");
        return "redirect:/usuarios";
    }

    @GetMapping("/funcionarios")
    public String funcionarios(Model model) {
        List<Usuario> funcionarios = usuarioRepository.findAll().stream()
                .filter(u -> "gerente".equalsIgnoreCase(u.getRole()) ||
                        "atendente".equalsIgnoreCase(u.getRole()))
                .collect(Collectors.toList());
        model.addAttribute("funcionarios", funcionarios);
        model.addAttribute("content", "funcionarios/lista :: content");
        return "layout";
    }

    @GetMapping("/entregadores")
    public String entregadores(Model model) {
        model.addAttribute("entregadores", entregadorService.listarTodos());
        model.addAttribute("content", "entregadores/lista :: content");
        return "layout";
    }

    @PostMapping("/entregadores")
    public String cadastrarEntregador(@org.springframework.web.bind.annotation.RequestParam String nome,
            RedirectAttributes redirectAttributes) {
        try {
            entregadorService.cadastrar(nome);
            redirectAttributes.addFlashAttribute("sucesso", "Entregador cadastrado com sucesso.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/entregadores";
    }

    // ========================
    // PEDIDOS
    // ========================
    @GetMapping("/pedidos")
    public String pedidos(Model model) {
        List<Pedido> pedidos = pedidoService.listarPedidos();
        model.addAttribute("pedidos", pedidos);
        var autenticacao = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication();
        boolean atendente = autenticacao.getAuthorities().stream()
                .anyMatch(p -> p.getAuthority().equals("ROLE_atendente"));
        boolean gerente = autenticacao.getAuthorities().stream().anyMatch(p -> p.getAuthority().equals("ROLE_gerente"));
        model.addAttribute("podeSolicitarCancelamento", atendente);
        model.addAttribute("podeAprovarCancelamento", atendente || gerente);
        model.addAttribute("podeCriarPedido", atendente || gerente);
        model.addAttribute("podeAtualizarPedido", gerente);
        model.addAttribute("produtosPedido", produtoService.listarProdutos());
        model.addAttribute("entregadoresPedido", entregadorService.listarTodos());
        model.addAttribute("podeAtribuirEntregador", atendente || gerente);
        model.addAttribute("content", "pedidos :: content");
        log.info("Carregando fragmento: pedidos :: content");
        return "layout";
    }

    // ========================
    // PRODUTOS
    // ========================
    @GetMapping("/produtos")
    public String produtos(Model model) {
        List<Produto> produtos = produtoService.listarProdutos();
        model.addAttribute("produtos", produtos);
        model.addAttribute("content", "produtos/lista :: content");
        log.info("Carregando fragmento: produtos/lista :: content");
        return "layout";
    }

    // ========================
    // CLIENTES
    // ========================
    @GetMapping("/clientes")
    public String clientes(Model model, Authentication authentication) {

        boolean podeExcluirCliente = authentication != null &&
                authentication.getAuthorities().stream()
                        .anyMatch(p -> p.getAuthority().equals("ROLE_admin"));

        boolean podeEditarCliente = authentication != null &&
                authentication.getAuthorities().stream()
                        .anyMatch(p -> p.getAuthority().equals("ROLE_atendente"));

        model.addAttribute("podeExcluirCliente", podeExcluirCliente);
        model.addAttribute("podeEditarCliente", podeEditarCliente);

        List<Cliente> clientes = clienteService.listarClientes();
        model.addAttribute("clientes", clientes);
        model.addAttribute("content", "clientes/lista :: content");

        log.info("Carregando fragmento: clientes/lista :: content");

        return "layout";
    }

    @GetMapping("/clientes/novo")
    public String novoClienteForm(Model model) {
        model.addAttribute("cliente", new Cliente());
        model.addAttribute("content", "clientes/form :: content");
        return "layout";
    }

    @PostMapping("/clientes/salvar")
    public String salvarCliente(@ModelAttribute Cliente cliente,
            RedirectAttributes redirectAttributes) {
        clienteService.salvar(cliente);
        redirectAttributes.addFlashAttribute("sucesso", "Cliente criado com sucesso!");
        return "redirect:/clientes";
    }

    @GetMapping("/clientes/editar/{id}")
    public String editarClienteForm(@PathVariable String id, Model model,
            RedirectAttributes redirectAttributes) {
        boolean encontrado = clienteService.buscarPorId(id).map(c -> {
            model.addAttribute("cliente", c);
            return true;
        }).orElse(false);

        if (!encontrado) {
            redirectAttributes.addFlashAttribute("erro", "Cliente não encontrado.");
            return "redirect:/clientes";
        }

        model.addAttribute("content", "clientes/form :: content");
        return "layout";
    }

    @PostMapping("/clientes/editar/{id}")
    public String atualizarCliente(@PathVariable String id,
            @ModelAttribute Cliente cliente,
            RedirectAttributes redirectAttributes) {
        cliente.setId(id);
        clienteService.salvar(cliente);
        redirectAttributes.addFlashAttribute("sucesso", "Cliente atualizado com sucesso!");
        return "redirect:/clientes";
    }

    // ========================
    // OUTRAS ROTAS
    // ========================
    @GetMapping("/configuracoes")
    public String configuracoes(Model model) {
        model.addAttribute("content", "configuracoes :: content");
        return "layout";
    }

    @GetMapping("/teste")
    public String teste() {
        return "teste";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }
}
