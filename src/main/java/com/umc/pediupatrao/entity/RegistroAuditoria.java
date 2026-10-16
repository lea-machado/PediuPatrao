package com.umc.pediupatrao.entity;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "auditoria")
public class RegistroAuditoria {
    @Id
    private String id;
    private String usuario;
    private String perfil;
    private String aprovadoPor;
    private LocalDateTime dataHora;
    private String operacao;
    private String entidade;
    private String entidadeId;
    private Map<String, Object> anterior = new LinkedHashMap<>();
    private Map<String, Object> novo = new LinkedHashMap<>();
    private String justificativa;

    public String getId() {
        return id;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String v) {
        usuario = v;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String v) {
        perfil = v;
    }

    public String getAprovadoPor() {
        return aprovadoPor;
    }

    public void setAprovadoPor(String aprovadoPor) {
        this.aprovadoPor = aprovadoPor;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime v) {
        dataHora = v;
    }

    public String getOperacao() {
        return operacao;
    }

    public void setOperacao(String v) {
        operacao = v;
    }

    public String getEntidade() {
        return entidade;
    }

    public void setEntidade(String v) {
        entidade = v;
    }

    public String getEntidadeId() {
        return entidadeId;
    }

    public void setEntidadeId(String v) {
        entidadeId = v;
    }

    public Map<String, Object> getAnterior() {
        return anterior;
    }

    public void setAnterior(Map<String, Object> v) {
        anterior = v;
    }

    public Map<String, Object> getNovo() {
        return novo;
    }

    public void setNovo(Map<String, Object> v) {
        novo = v;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public void setJustificativa(String v) {
        justificativa = v;
    }
}