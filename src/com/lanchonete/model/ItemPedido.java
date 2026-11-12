package com.lanchonete.model;

import java.math.BigDecimal;

/**
 * Entidade Item do Pedido: associa um Produto a um Pedido, com a quantidade
 * e o valor praticado no momento da venda. Cada item pertence a um unico
 * pedido e referencia um unico produto.
 */
public class ItemPedido {

    private int id;
    private int pedidoId;
    private Produto produto;
    private int quantidade;
    private BigDecimal valorUnitario;
    private BigDecimal subtotal;

    public ItemPedido() {
    }

    public ItemPedido(Produto produto, int quantidade, BigDecimal valorUnitario) {
        this.produto = produto;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
        recalcularSubtotal();
    }

    public final void recalcularSubtotal() {
        if (valorUnitario != null) {
            this.subtotal = valorUnitario.multiply(BigDecimal.valueOf(quantidade));
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(int pedidoId) {
        this.pedidoId = pedidoId;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
        recalcularSubtotal();
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(BigDecimal valorUnitario) {
        this.valorUnitario = valorUnitario;
        recalcularSubtotal();
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
}
