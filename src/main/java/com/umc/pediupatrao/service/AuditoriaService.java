package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.RegistroAuditoria;
import com.umc.pediupatrao.repository.RegistroAuditoriaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuditoriaService {
    @Autowired
    private RegistroAuditoriaRepository repository;

    public void registrar(String operacao, String entidade, String entidadeId,
            Map<String, Object> anterior, Map<String, Object> novo,
            String justificativa) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        RegistroAuditoria registro = new RegistroAuditoria();
        registro.setUsuario(auth == null ? "SISTEMA" : auth.getName());
        registro.setPerfil(auth == null ? "SISTEMA"
                : auth.getAuthorities().stream()
                        .map(a -> a.getAuthority()).filter(a -> a.startsWith("ROLE_"))
                        .findFirst().orElse("SISTEMA").replace("ROLE_", ""));
        registro.setDataHora(LocalDateTime.now());
        registro.setOperacao(operacao);
        registro.setEntidade(entidade);
        registro.setEntidadeId(entidadeId);
        registro.setAnterior(anterior);
        registro.setNovo(novo);
        registro.setJustificativa(justificativa);
        repository.save(registro);
    }

    public void registrarGerente(String operacao, String entidade, String entidadeId,
            String gerente, Map<String, Object> anterior,
            Map<String, Object> novo, String justificativa) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        RegistroAuditoria registro = new RegistroAuditoria();
        registro.setUsuario(auth == null ? "SISTEMA" : auth.getName());
        registro.setPerfil(auth == null ? "SISTEMA"
                : auth.getAuthorities().stream()
                        .map(a -> a.getAuthority()).filter(a -> a.startsWith("ROLE_"))
                        .findFirst().orElse("SISTEMA").replace("ROLE_", ""));
        registro.setAprovadoPor(gerente);
        registro.setDataHora(LocalDateTime.now());
        registro.setOperacao(operacao);
        registro.setEntidade(entidade);
        registro.setEntidadeId(entidadeId);
        registro.setAnterior(anterior);
        registro.setNovo(novo);
        registro.setJustificativa(justificativa);
        repository.save(registro);
    }

    public List<RegistroAuditoria> listar() {
        return repository.findAll();
    }
}