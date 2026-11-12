package com.lanchonete.dao;

import com.lanchonete.model.Cliente;
import com.lanchonete.model.ItemPedido;
import com.lanchonete.model.Pedido;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO responsavel pela entidade Pedido (operacao principal) e pela
 * orquestracao da gravacao dos seus itens dentro de uma transacao, para
 * preservar a consistencia do relacionamento Pedido -> Itens.
 */
public class PedidoDAO {

    private final ItemPedidoDAO itemPedidoDAO = new ItemPedidoDAO();

    /**
     * Insere um novo pedido e todos os seus itens em uma unica transacao.
     * Se qualquer etapa falhar, a transacao inteira e desfeita (rollback).
     */
    public void inserir(Pedido pedido) throws SQLException {
        if (pedido.getItens() == null || pedido.getItens().isEmpty()) {
            throw new SQLException("Um pedido deve conter pelo menos um item.");
        }

        Connection con = null;
        try {
            con = ConexaoBD.getConexao();
            con.setAutoCommit(false);

            String sql = "INSERT INTO pedido (cliente_id, data_hora, forma_pagamento, status, valor_total) "
                    + "VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, pedido.getCliente().getId());
                stmt.setTimestamp(2, Timestamp.valueOf(pedido.getDataHora()));
                stmt.setString(3, pedido.getFormaPagamento());
                stmt.setString(4, pedido.getStatus());
                stmt.setBigDecimal(5, pedido.getValorTotal());
                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        pedido.setId(rs.getInt(1));
                    }
                }
            }

            for (ItemPedido item : pedido.getItens()) {
                item.setPedidoId(pedido.getId());
                itemPedidoDAO.inserir(con, item);
            }

            con.commit();
        } catch (SQLException e) {
            if (con != null) {
                con.rollback();
            }
            throw e;
        } finally {
            if (con != null) {
                con.setAutoCommit(true);
                ConexaoBD.fechar(con);
            }
        }
    }

    /**
     * Atualiza os dados do pedido e regrava seus itens (remove os antigos e
     * insere a lista atual), tudo em uma unica transacao.
     */
    public void atualizar(Pedido pedido) throws SQLException {
        if (pedido.getItens() == null || pedido.getItens().isEmpty()) {
            throw new SQLException("Um pedido deve conter pelo menos um item.");
        }

        Connection con = null;
        try {
            con = ConexaoBD.getConexao();
            con.setAutoCommit(false);

            String sql = "UPDATE pedido SET cliente_id = ?, forma_pagamento = ?, status = ?, "
                    + "valor_total = ? WHERE id = ?";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setInt(1, pedido.getCliente().getId());
                stmt.setString(2, pedido.getFormaPagamento());
                stmt.setString(3, pedido.getStatus());
                stmt.setBigDecimal(4, pedido.getValorTotal());
                stmt.setInt(5, pedido.getId());
                stmt.executeUpdate();
            }

            itemPedidoDAO.excluirPorPedido(con, pedido.getId());
            for (ItemPedido item : pedido.getItens()) {
                item.setPedidoId(pedido.getId());
                itemPedidoDAO.inserir(con, item);
            }

            con.commit();
        } catch (SQLException e) {
            if (con != null) {
                con.rollback();
            }
            throw e;
        } finally {
            if (con != null) {
                con.setAutoCommit(true);
                ConexaoBD.fechar(con);
            }
        }
    }

    /**
     * Exclui um pedido. Os itens sao removidos automaticamente pela
     * restricao ON DELETE CASCADE definida no banco de dados.
     */
    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM pedido WHERE id = ?";
        try (Connection con = ConexaoBD.getConexao();
                PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public Pedido buscarPorId(int id) throws SQLException {
        String sql = "SELECT p.*, c.nome AS cliente_nome, c.telefone AS cliente_telefone, "
                + "c.endereco AS cliente_endereco, c.email AS cliente_email "
                + "FROM pedido p JOIN cliente c ON c.id = p.cliente_id WHERE p.id = ?";
        try (Connection con = ConexaoBD.getConexao();
                PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Pedido pedido = mapear(rs);
                    pedido.setItens(itemPedidoDAO.listarPorPedido(pedido.getId()));
                    return pedido;
                }
            }
        }
        return null;
    }

    /**
     * Lista todos os pedidos (sem carregar os itens, para ser mais leve na
     * tela de listagem). Os itens sao carregados sob demanda ao abrir um
     * pedido especifico.
     */
    public List<Pedido> listarTodos() throws SQLException {
        List<Pedido> lista = new ArrayList<>();
        String sql = "SELECT p.*, c.nome AS cliente_nome, c.telefone AS cliente_telefone, "
                + "c.endereco AS cliente_endereco, c.email AS cliente_email "
                + "FROM pedido p JOIN cliente c ON c.id = p.cliente_id "
                + "ORDER BY p.data_hora DESC";
        try (Connection con = ConexaoBD.getConexao();
                PreparedStatement stmt = con.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    private Pedido mapear(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente();
        cliente.setId(rs.getInt("cliente_id"));
        cliente.setNome(rs.getString("cliente_nome"));
        cliente.setTelefone(rs.getString("cliente_telefone"));
        cliente.setEndereco(rs.getString("cliente_endereco"));
        cliente.setEmail(rs.getString("cliente_email"));

        Pedido pedido = new Pedido();
        pedido.setId(rs.getInt("id"));
        pedido.setCliente(cliente);
        pedido.setDataHora(rs.getTimestamp("data_hora").toLocalDateTime());
        pedido.setFormaPagamento(rs.getString("forma_pagamento"));
        pedido.setStatus(rs.getString("status"));
        pedido.setValorTotal(rs.getBigDecimal("valor_total"));
        return pedido;
    }
}
