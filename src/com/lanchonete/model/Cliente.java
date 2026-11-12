package com.lanchonete.model;

/**
 * Entidade Participante do sistema: representa o cliente que realiza pedidos.
 */
public class Cliente {

    private int id;
    private String nome;
    private String telefone;
    private String endereco;
    private String email;

    public Cliente() {
    }

    public Cliente(int id, String nome, String telefone, String endereco, String email) {
        this.id = id;
        this.nome = nome;
        this.telefone = telefone;
        this.endereco = endereco;
        this.email = email;
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

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Usado para exibir o cliente de forma amigavel em JComboBox.
     */
    @Override
    public String toString() {
        return id + " - " + nome;
    }
}
