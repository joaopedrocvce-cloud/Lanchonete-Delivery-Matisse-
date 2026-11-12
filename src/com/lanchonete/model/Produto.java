package com.lanchonete.model;

import java.math.BigDecimal;

/**
 * Entidade Produto (recurso vendavel) do sistema. Representa um item do
 * cardapio da lanchonete que pode ser associado a pedidos.
 */
public class Produto {

    private int id;
    private String nome;
    private String descricao;
    private BigDecimal preco;
    private String categoria;
    private String imagemCaminho;

    public Produto() {
    }

    public Produto(int id, String nome, String descricao, BigDecimal preco,
            String categoria, String imagemCaminho) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.categoria = categoria;
        this.imagemCaminho = imagemCaminho;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getImagemCaminho() {
        return imagemCaminho;
    }

    public void setImagemCaminho(String imagemCaminho) {
        this.imagemCaminho = imagemCaminho;
    }

    /**
     * Usado para exibir o produto de forma amigavel em JComboBox.
     */
    @Override
    public String toString() {
        return id + " - " + nome + " (R$ " + preco + ")";
    }
}
