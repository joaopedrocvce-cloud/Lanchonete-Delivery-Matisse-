package com.lanchonete.dao;

import com.lanchonete.model.Produto;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO responsavel pelo CRUD da entidade Produto.
 */
public class ProdutoDAO {

    public void inserir(Produto produto) throws SQLException {
        String sql = "INSERT INTO produto (nome, descricao, preco, categoria, imagem_caminho) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = ConexaoBD.getConexao();
                PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, produto.getNome());
            stmt.setString(2, produto.getDescricao());
            stmt.setBigDecimal(3, produto.getPreco());
            stmt.setString(4, produto.getCategoria());
            stmt.setString(5, produto.getImagemCaminho());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    produto.setId(rs.getInt(1));
                }
            }
        }
    }

    public void atualizar(Produto produto) throws SQLException {
        String sql = "UPDATE produto SET nome = ?, descricao = ?, preco = ?, categoria = ?, "
                + "imagem_caminho = ? WHERE id = ?";
        try (Connection con = ConexaoBD.getConexao();
                PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, produto.getNome());
            stmt.setString(2, produto.getDescricao());
            stmt.setBigDecimal(3, produto.getPreco());
            stmt.setString(4, produto.getCategoria());
            stmt.setString(5, produto.getImagemCaminho());
            stmt.setInt(6, produto.getId());
            stmt.executeUpdate();
        }
    }

    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM produto WHERE id = ?";
        try (Connection con = ConexaoBD.getConexao();
                PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public Produto buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM produto WHERE id = ?";
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

    public List<Produto> listarTodos() throws SQLException {
        List<Produto> lista = new ArrayList<>();
        String sql = "SELECT * FROM produto ORDER BY nome";
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
     * Verifica se o produto ja foi usado em algum item de pedido. Util para
     * impedir exclusao de produtos referenciados, preservando a
     * consistencia do relacionamento.
     */
    public boolean estaEmUso(int produtoId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM item_pedido WHERE produto_id = ?";
        try (Connection con = ConexaoBD.getConexao();
                PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, produtoId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private Produto mapear(ResultSet rs) throws SQLException {
        Produto p = new Produto();
        p.setId(rs.getInt("id"));
        p.setNome(rs.getString("nome"));
        p.setDescricao(rs.getString("descricao"));
        p.setPreco(rs.getBigDecimal("preco"));
        p.setCategoria(rs.getString("categoria"));
        p.setImagemCaminho(rs.getString("imagem_caminho"));
        return p;
    }
}
