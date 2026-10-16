package com.umc.pediupatrao.entity;

import jakarta.persistence.Entity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "pedidos")
@Entity
public class Pedido {
    @Id
    private String id;
    private String clienteId;
    private String tipoPizza;
    private String status;
    private int quantidade;
    private String sabor;

    private List<ItemPedido> itens = new ArrayList<>();
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal percentualDesconto = BigDecimal.ZERO;
    private BigDecimal descontoSolicitado;
    private String descontoSolicitadoPor;
    private LocalDateTime dataSolicitacaoDesconto;
    private String descontoAprovadoPor;
    private LocalDateTime dataAprovacaoDesconto;
    private BigDecimal desconto = BigDecimal.ZERO;
    private BigDecimal valorTotal = BigDecimal.ZERO;
    private String registradoPor;
    private LocalDateTime dataEntrada;

    private LocalDateTime dataSaida;
    private String entregadorId;
    private String responsavelSaida;
    private String justificativaCancelamento;
    private String cancelamentoSolicitadoPor;
    private String cancelamentoAprovadoPor;
    private LocalDateTime dataCancelamento;
    private String statusAntesCancelamento;
    private List<String> auditoria = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClienteId() {
        return clienteId;
    }

    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }

    public String getTipoPizza() {
        return tipoPizza;
    }

    public void setTipoPizza(String tipoPizza) {
        this.tipoPizza = tipoPizza;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public String getSabor() {
        return sabor;
    }

    public void setSabor(String sabor) {
        this.sabor = sabor;
    }

    public List<ItemPedido> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedido> itens) {
        this.itens = itens;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getPercentualDesconto() {
        return percentualDesconto;
    }

    public void setPercentualDesconto(BigDecimal percentualDesconto) {
        this.percentualDesconto = percentualDesconto;
    }

    public BigDecimal getDescontoSolicitado() {
        return descontoSolicitado;
    }

    public void setDescontoSolicitado(BigDecimal valor) {
        this.descontoSolicitado = valor;
    }

    public String getDescontoSolicitadoPor() {
        return descontoSolicitadoPor;
    }

    public void setDescontoSolicitadoPor(String valor) {
        this.descontoSolicitadoPor = valor;
    }

    public LocalDateTime getDataSolicitacaoDesconto() {
        return dataSolicitacaoDesconto;
    }

    public void setDataSolicitacaoDesconto(LocalDateTime valor) {
        this.dataSolicitacaoDesconto = valor;
    }

    public String getDescontoAprovadoPor() {
        return descontoAprovadoPor;
    }

    public void setDescontoAprovadoPor(String valor) {
        this.descontoAprovadoPor = valor;
    }

    public LocalDateTime getDataAprovacaoDesconto() {
        return dataAprovacaoDesconto;
    }

    public void setDataAprovacaoDesconto(LocalDateTime valor) {
        this.dataAprovacaoDesconto = valor;
    }

    public BigDecimal getDesconto() {
        return desconto;
    }

    public void setDesconto(BigDecimal desconto) {
        this.desconto = desconto;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public String getRegistradoPor() {
        return registradoPor;
    }

    public void setRegistradoPor(String registradoPor) {
        this.registradoPor = registradoPor;
    }

    public String getEntregadorId() {
        return entregadorId;
    }

    public void setEntregadorId(String entregadorId) {
        this.entregadorId = entregadorId;
    }

    public String getResponsavelSaida() {
        return responsavelSaida;
    }

    public void setResponsavelSaida(String responsavelSaida) {
        this.responsavelSaida = responsavelSaida;
    }

    public String getJustificativaCancelamento() {
        return justificativaCancelamento;
    }

    public void setJustificativaCancelamento(String justificativaCancelamento) {
        this.justificativaCancelamento = justificativaCancelamento;
    }

    public String getCancelamentoSolicitadoPor() {
        return cancelamentoSolicitadoPor;
    }

    public void setCancelamentoSolicitadoPor(String cancelamentoSolicitadoPor) {
        this.cancelamentoSolicitadoPor = cancelamentoSolicitadoPor;
    }

    public String getCancelamentoAprovadoPor() {
        return cancelamentoAprovadoPor;
    }

    public void setCancelamentoAprovadoPor(String cancelamentoAprovadoPor) {
        this.cancelamentoAprovadoPor = cancelamentoAprovadoPor;
    }

    public String getStatusAntesCancelamento() {
        return statusAntesCancelamento;
    }

    public void setStatusAntesCancelamento(String statusAntesCancelamento) {
        this.statusAntesCancelamento = statusAntesCancelamento;
    }

    public LocalDateTime getDataEntrada() {
        return dataEntrada;
    }

    public void setDataEntrada(LocalDateTime dataEntrada) {
        this.dataEntrada = dataEntrada;
    }

    public LocalDateTime getDataSaida() {
        return dataSaida;
    }

    public void setDataSaida(LocalDateTime dataSaida) {
        this.dataSaida = dataSaida;
    }

    public LocalDateTime getDataCancelamento() {
        return dataCancelamento;
    }

    public void setDataCancelamento(LocalDateTime dataCancelamento) {
        this.dataCancelamento = dataCancelamento;
    }

    public List<String> getAuditoria() {
        return auditoria;
    }

    public void setAuditoria(List<String> auditoria) {
        this.auditoria = auditoria;
    }
}
