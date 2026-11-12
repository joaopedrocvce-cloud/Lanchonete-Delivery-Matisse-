package com.lanchonete.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade Operacao Principal do sistema: representa um pedido feito por um
 * cliente. Cada pedido esta vinculado a exatamente um cliente e contem uma
 * lista de itens (produtos + quantidade).
 */
public class Pedido {

    public static final String STATUS_EM_PREPARO = "Em preparo";
    public static final String STATUS_SAIU_ENTREGA = "Saiu para entrega";
    public static final String STATUS_ENTREGUE = "Entregue";
    public static final String STATUS_CANCELADO = "Cancelado";

    private int id;
    private Cliente cliente;
    private LocalDateTime dataHora;
    private String formaPagamento;
    private String status;
    private BigDecimal valorTotal;
    private List<ItemPedido> itens;

    public Pedido() {
        this.itens = new ArrayList<>();
        this.dataHora = LocalDateTime.now();
        this.status = STATUS_EM_PREPARO;
        this.valorTotal = BigDecimal.ZERO;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public List<ItemPedido> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedido> itens) {
        this.itens = itens;
    }

    /**
     * Recalcula o valor total do pedido somando o subtotal de cada item.
     * Deve ser chamado sempre que a lista de itens for alterada.
     */
    public void recalcularTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemPedido item : itens) {
            total = total.add(item.getSubtotal());
        }
        this.valorTotal = total;
    }
}
