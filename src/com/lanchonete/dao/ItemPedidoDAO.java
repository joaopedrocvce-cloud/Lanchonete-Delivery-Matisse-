package com.lanchonete.dao;

import com.lanchonete.model.ItemPedido;
import com.lanchonete.model.Produto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO responsavel pelos itens de um pedido. Um item sempre pertence a um
 * unico pedido e referencia um unico produto.
 */
public class ItemPedidoDAO {

    /**
     * Insere um item vinculado a um pedido ja existente. Deve ser chamado
     * dentro da mesma conexao/transacao do pedido, quando possivel.
     */
    public void inserir(Connection con, ItemPedido item) throws SQLException {
        String sql = "INSERT INTO item_pedido (pedido_id, produto_id, quantidade, "
                + "valor_unitario, subtotal) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, item.getPedidoId());
            stmt.setInt(2, item.getProduto().getId());
            stmt.setInt(3, item.getQuantidade());
            stmt.setBigDecimal(4, item.getValorUnitario());
            stmt.setBigDecimal(5, item.getSubtotal());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    item.setId(rs.getInt(1));
                }
            }
        }
    }

    /**
     * Remove todos os itens de um pedido. Utilizado antes de regravar os
     * itens ao editar um pedido existente.
     */
    public void excluirPorPedido(Connection con, int pedidoId) throws SQLException {
        String sql = "DELETE FROM item_pedido WHERE pedido_id = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, pedidoId);
            stmt.executeUpdate();
        }
    }

    public List<ItemPedido> listarPorPedido(int pedidoId) throws SQLException {
        List<ItemPedido> lista = new ArrayList<>();
        String sql = "SELECT i.*, p.nome AS produto_nome, p.descricao AS produto_descricao, "
                + "p.categoria AS produto_categoria, p.imagem_caminho AS produto_imagem "
                + "FROM item_pedido i "
                + "JOIN produto p ON p.id = i.produto_id "
                + "WHERE i.pedido_id = ?";
        try (Connection con = ConexaoBD.getConexao();
                PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, pedidoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Produto produto = new Produto();
                    produto.setId(rs.getInt("produto_id"));
                    produto.setNome(rs.getString("produto_nome"));
                    produto.setDescricao(rs.getString("produto_descricao"));
                    produto.setCategoria(rs.getString("produto_categoria"));
                    produto.setImagemCaminho(rs.getString("produto_imagem"));
                    produto.setPreco(rs.getBigDecimal("valor_unitario"));

                    ItemPedido item = new ItemPedido();
                    item.setId(rs.getInt("id"));
                    item.setPedidoId(rs.getInt("pedido_id"));
                    item.setProduto(produto);
                    item.setQuantidade(rs.getInt("quantidade"));
                    item.setValorUnitario(rs.getBigDecimal("valor_unitario"));
                    item.setSubtotal(rs.getBigDecimal("subtotal"));
                    lista.add(item);
                }
            }
        }
        return lista;
    }
}
