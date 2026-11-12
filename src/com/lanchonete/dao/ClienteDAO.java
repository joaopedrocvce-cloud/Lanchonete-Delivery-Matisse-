package com.lanchonete.dao;

import com.lanchonete.model.Cliente;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) responsavel pelo CRUD da entidade Cliente.
 */
public class ClienteDAO {

    public void inserir(Cliente cliente) throws SQLException {
        String sql = "INSERT INTO cliente (nome, telefone, endereco, email) VALUES (?, ?, ?, ?)";
        try (Connection con = ConexaoBD.getConexao();
                PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, cliente.getNome());
            stmt.setString(2, cliente.getTelefone());
            stmt.setString(3, cliente.getEndereco());
            stmt.setString(4, cliente.getEmail());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    cliente.setId(rs.getInt(1));
                }
            }
        }
    }

    public void atualizar(Cliente cliente) throws SQLException {
        String sql = "UPDATE cliente SET nome = ?, telefone = ?, endereco = ?, email = ? WHERE id = ?";
        try (Connection con = ConexaoBD.getConexao();
                PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, cliente.getNome());
            stmt.setString(2, cliente.getTelefone());
            stmt.setString(3, cliente.getEndereco());
            stmt.setString(4, cliente.getEmail());
            stmt.setInt(5, cliente.getId());
            stmt.executeUpdate();
        }
    }

    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM cliente WHERE id = ?";
        try (Connection con = ConexaoBD.getConexao();
                PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public Cliente buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM cliente WHERE id = ?";
        try (Connection con = ConexaoBD.getConexao();
                PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    public List<Cliente> listarTodos() throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM cliente ORDER BY nome";
        try (Connection con = ConexaoBD.getConexao();
                PreparedStatement stmt = con.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    /**
     * Verifica se o cliente possui pedidos vinculados. Util para impedir
     * exclusao de clientes que ja possuem historico de pedidos, preservando
     * a consistencia do relacionamento.
     */
    public boolean possuiPedidos(int clienteId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM pedido WHERE cliente_id = ?";
        try (Connection con = ConexaoBD.getConexao();
                PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, clienteId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setId(rs.getInt("id"));
        c.setNome(rs.getString("nome"));
        c.setTelefone(rs.getString("telefone"));
        c.setEndereco(rs.getString("endereco"));
        c.setEmail(rs.getString("email"));
        return c;
    }
}
