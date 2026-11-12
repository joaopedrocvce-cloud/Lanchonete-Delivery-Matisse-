/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lanchonete.view;

import com.lanchonete.dao.ClienteDAO;
import com.lanchonete.dao.PedidoDAO;
import com.lanchonete.dao.ProdutoDAO;
import com.lanchonete.model.Cliente;
import com.lanchonete.model.ItemPedido;
import com.lanchonete.model.Pedido;
import com.lanchonete.model.Produto;
import com.lanchonete.util.EmailUtil;
import com.lanchonete.util.PdfUtil;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author aluno
 */
public class TelaPedido extends javax.swing.JFrame {

    /**
     * Creates new form TelaPedido
     */
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String[] FORMAS_PAGAMENTO = {"Dinheiro", "Cartao de Credito", "Cartao de Debito", "Pix"};
    private static final String[] STATUS_PEDIDO = {
        Pedido.STATUS_EM_PREPARO, Pedido.STATUS_SAIU_ENTREGA, Pedido.STATUS_ENTREGUE, Pedido.STATUS_CANCELADO
    };

    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private final PedidoDAO pedidoDAO = new PedidoDAO();

    private final Integer idPedido;
    private Pedido pedido;
    private final List<ItemPedido> itens = new ArrayList<>();
    private DefaultTableModel modeloItens;

    public TelaPedido(JFrame pai, Integer idPedido) {
        this.idPedido = idPedido;
        initComponents();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle(idPedido == null ? "Novo Pedido" : "Editar Pedido #" + idPedido);

        cmbFormaPagamento.setModel(new DefaultComboBoxModel<>(FORMAS_PAGAMENTO));
        cmbStatus.setModel(new DefaultComboBoxModel<>(STATUS_PEDIDO));
        spnQuantidade.setModel(new SpinnerNumberModel(1, 1, 999, 1));

        String[] colunas = {"Produto", "Quantidade", "Valor Unitario", "Subtotal"};
        modeloItens = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelaItens.setModel(modeloItens);

        setLocationRelativeTo(pai);
        carregarCombos();

        if (idPedido == null) {
            pedido = new Pedido();
            lblData.setText(pedido.getDataHora().format(FORMATO_DATA));
        } else {
            carregarPedidoExistente();
        }
    }

    private void carregarCombos() {
        try {
            List<Cliente> clientes = clienteDAO.listarTodos();
            cmbCliente.removeAllItems();
            for (Cliente c : clientes) {
                cmbCliente.addItem(c);
            }

            List<Produto> produtos = produtoDAO.listarTodos();
            cmbProduto.removeAllItems();
            for (Produto p : produtos) {
                cmbProduto.addItem(p);
            }
        } catch (SQLException e) {
            exibirErroBanco(e);
        }
    }

    private void carregarPedidoExistente() {
        try {
            pedido = pedidoDAO.buscarPorId(idPedido);
            if (pedido == null) {
                JOptionPane.showMessageDialog(this, "Pedido nao encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
                dispose();
                return;
            }

            selecionarClienteNoCombo(pedido.getCliente().getId());
            cmbFormaPagamento.setSelectedItem(pedido.getFormaPagamento());
            cmbStatus.setSelectedItem(pedido.getStatus());
            lblData.setText(pedido.getDataHora().format(FORMATO_DATA));

            itens.clear();
            itens.addAll(pedido.getItens());
            atualizarTabelaItens();
        } catch (SQLException e) {
            exibirErroBanco(e);
        }
    }

    private void selecionarClienteNoCombo(int clienteId) {
        for (int i = 0; i < cmbCliente.getItemCount(); i++) {
            Cliente c = cmbCliente.getItemAt(i);
            if (c.getId() == clienteId) {
                cmbCliente.setSelectedIndex(i);
                return;
            }
        }
    }

    private void adicionarItem() {
        Produto produtoSelecionado = (Produto) cmbProduto.getSelectedItem();
        if (produtoSelecionado == null) {
            JOptionPane.showMessageDialog(this,
                    "Nao ha produtos cadastrados. Cadastre um produto antes de montar o pedido.",
                    "Atencao", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int quantidade = (Integer) spnQuantidade.getValue();

        for (ItemPedido itemExistente : itens) {
            if (itemExistente.getProduto().getId() == produtoSelecionado.getId()) {
                itemExistente.setQuantidade(itemExistente.getQuantidade() + quantidade);
                atualizarTabelaItens();
                return;
            }
        }

        ItemPedido novoItem = new ItemPedido(produtoSelecionado, quantidade, produtoSelecionado.getPreco());
        itens.add(novoItem);
        atualizarTabelaItens();
    }

    private void removerItemSelecionado() {
        int linha = tabelaItens.getSelectedRow();
        if (linha == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um item da tabela para remover.",
                    "Atencao", JOptionPane.WARNING_MESSAGE);
            return;
        }
        itens.remove(linha);
        atualizarTabelaItens();
    }

    private void atualizarTabelaItens() {
        modeloItens.setRowCount(0);
        BigDecimal total = BigDecimal.ZERO;
        for (ItemPedido item : itens) {
            modeloItens.addRow(new Object[]{
                item.getProduto().getNome(),
                item.getQuantidade(),
                "R$ " + item.getValorUnitario(),
                "R$ " + item.getSubtotal()
            });
            total = total.add(item.getSubtotal());
        }
        lblTotal.setText("Total: R$ " + total);
    }

    private boolean validarFormulario() {
        if (cmbCliente.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this,
                    "Nao ha clientes cadastrados. Cadastre um cliente antes de montar o pedido.",
                    "Validacao", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (itens.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Adicione pelo menos um item ao pedido.", "Validacao", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void salvarPedido() {
        if (!validarFormulario()) {
            return;
        }

        pedido.setCliente((Cliente) cmbCliente.getSelectedItem());
        pedido.setFormaPagamento((String) cmbFormaPagamento.getSelectedItem());
        pedido.setStatus((String) cmbStatus.getSelectedItem());
        pedido.setItens(itens);
        pedido.recalcularTotal();

        try {
            if (idPedido == null && pedido.getId() == 0) {
                pedidoDAO.inserir(pedido);
                setTitle("Editar Pedido #" + pedido.getId());
                JOptionPane.showMessageDialog(this,
                        "Pedido #" + pedido.getId() + " cadastrado com sucesso!");
            } else {
                pedidoDAO.atualizar(pedido);
                JOptionPane.showMessageDialog(this, "Pedido atualizado com sucesso!");
            }
        } catch (SQLException e) {
            exibirErroBanco(e);
        }
    }

    private void gerarPdf() {
        if (pedido == null || pedido.getId() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Salve o pedido antes de gerar o comprovante em PDF.",
                    "Atencao", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser seletor = new JFileChooser();
        seletor.setSelectedFile(new File("comprovante_pedido_" + pedido.getId() + ".pdf"));
        int resultado = seletor.showSaveDialog(this);
        if (resultado != JFileChooser.APPROVE_OPTION) {
            return;
        }

        String caminho = seletor.getSelectedFile().getAbsolutePath();
        if (!caminho.toLowerCase().endsWith(".pdf")) {
            caminho += ".pdf";
        }

        try {
            PdfUtil.gerarComprovante(pedido, caminho);
            JOptionPane.showMessageDialog(this, "Comprovante gerado com sucesso em:\n" + caminho);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao gerar o PDF:\n" + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void enviarEmail() {
        if (pedido == null || pedido.getId() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Salve o pedido antes de enviar o comprovante por e-mail.",
                    "Atencao", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String emailPadrao = pedido.getCliente().getEmail();
        String destinatario = JOptionPane.showInputDialog(this,
                "Informe o e-mail de destino:", emailPadrao);
        if (destinatario == null || destinatario.trim().isEmpty()) {
            return;
        }

        try {
            Path pasta = Paths.get("comprovantes");
            if (!Files.exists(pasta)) {
                Files.createDirectories(pasta);
            }
            String caminhoPdf = pasta.resolve("comprovante_pedido_" + pedido.getId() + ".pdf").toString();
            PdfUtil.gerarComprovante(pedido, caminhoPdf);

            String assunto = "Comprovante do seu pedido #" + pedido.getId() + " - Lanchonete Delivery";
            String corpo = "Ola, " + pedido.getCliente().getNome() + "!\n\n"
                    + "Segue em anexo o comprovante do seu pedido #" + pedido.getId() + ".\n"
                    + "Valor total: R$ " + pedido.getValorTotal() + "\n\n"
                    + "Obrigado pela preferencia!";

            EmailUtil.enviarComprovante(destinatario.trim(), assunto, corpo, caminhoPdf);
            JOptionPane.showMessageDialog(this, "E-mail enviado com sucesso para " + destinatario);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao gerar o PDF para anexar ao e-mail:\n" + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao enviar o e-mail. Verifique as configuracoes de SMTP em EmailUtil.java.\n"
                    + "Detalhe: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exibirErroBanco(SQLException e) {
        JOptionPane.showMessageDialog(this,
                "Erro ao acessar o banco de dados:\n" + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        lblData = new javax.swing.JLabel();
        cmbCliente = new javax.swing.JComboBox<>();
        cmbFormaPagamento = new javax.swing.JComboBox<>();
        cmbStatus = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        cmbProduto = new javax.swing.JComboBox<>();
        spnQuantidade = new javax.swing.JSpinner();
        btnAdicionar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelaItens = new javax.swing.JTable();
        btnRemover = new javax.swing.JButton();
        lblTotal = new javax.swing.JLabel();
        btnSalvar = new javax.swing.JButton();
        btnGerarPdf = new javax.swing.JButton();
        btnEnviarEmail = new javax.swing.JButton();
        btnFechar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Cliente");

        jLabel2.setText("Data/Hora");

        jLabel3.setText("Forma Pagamento");

        jLabel4.setText("Status");

        lblData.setText("-");

        cmbCliente.setModel(new javax.swing.DefaultComboBoxModel<Cliente>());

        cmbFormaPagamento.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        cmbStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel6.setText("Produto");

        jLabel7.setText("Quantidade");

        cmbProduto.setModel(new javax.swing.DefaultComboBoxModel<Produto>());

        btnAdicionar.setText("Adicionar Item");
        btnAdicionar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAdicionarActionPerformed(evt);
            }
        });

        tabelaItens.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(tabelaItens);

        btnRemover.setText("Remover Item");
        btnRemover.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoverActionPerformed(evt);
            }
        });

        lblTotal.setText("R$ 0.00");

        btnSalvar.setText("Salvar Pedido");
        btnSalvar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSalvarActionPerformed(evt);
            }
        });

        btnGerarPdf.setText("Gerar PDF");
        btnGerarPdf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGerarPdfActionPerformed(evt);
            }
        });

        btnEnviarEmail.setText("Enviar por E-mail");
        btnEnviarEmail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEnviarEmailActionPerformed(evt);
            }
        });

        btnFechar.setText("Fechar");
        btnFechar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFecharActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(36, 36, 36)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel1))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(cmbCliente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblData))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(lblTotal))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jLabel3)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(cmbFormaPagamento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jLabel4)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(cmbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnSalvar)
                                .addGap(146, 146, 146)
                                .addComponent(btnGerarPdf)
                                .addGap(156, 156, 156)
                                .addComponent(btnEnviarEmail)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnFechar))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel6)
                                        .addGap(18, 18, 18)
                                        .addComponent(cmbProduto, javax.swing.GroupLayout.PREFERRED_SIZE, 219, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(btnRemover)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                            .addComponent(jLabel7)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(spnQuantidade, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(btnAdicionar, javax.swing.GroupLayout.Alignment.LEADING)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 39, Short.MAX_VALUE)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 582, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(cmbCliente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTotal))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(lblData))
                        .addGap(14, 14, 14)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(cmbFormaPagamento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(cmbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(112, 112, 112)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(cmbProduto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(spnQuantidade, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(11, 11, 11)
                        .addComponent(btnAdicionar)
                        .addGap(18, 18, 18)
                        .addComponent(btnRemover)
                        .addGap(114, 114, 114))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addGap(53, 53, 53)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSalvar)
                    .addComponent(btnGerarPdf)
                    .addComponent(btnEnviarEmail)
                    .addComponent(btnFechar))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAdicionarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAdicionarActionPerformed
        adicionarItem();
    }//GEN-LAST:event_btnAdicionarActionPerformed

    private void btnRemoverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoverActionPerformed
        removerItemSelecionado();
    }//GEN-LAST:event_btnRemoverActionPerformed

    private void btnSalvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSalvarActionPerformed
        salvarPedido();
    }//GEN-LAST:event_btnSalvarActionPerformed

    private void btnGerarPdfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGerarPdfActionPerformed
        gerarPdf();
    }//GEN-LAST:event_btnGerarPdfActionPerformed

    private void btnEnviarEmailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEnviarEmailActionPerformed
        enviarEmail();
    }//GEN-LAST:event_btnEnviarEmailActionPerformed

    private void btnFecharActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFecharActionPerformed
        dispose();
    }//GEN-LAST:event_btnFecharActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(TelaPedido.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TelaPedido.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TelaPedido.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TelaPedido.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TelaPedido(null, null).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdicionar;
    private javax.swing.JButton btnEnviarEmail;
    private javax.swing.JButton btnFechar;
    private javax.swing.JButton btnGerarPdf;
    private javax.swing.JButton btnRemover;
    private javax.swing.JButton btnSalvar;
    private javax.swing.JComboBox<Cliente> cmbCliente;
    private javax.swing.JComboBox<String> cmbFormaPagamento;
    private javax.swing.JComboBox<Produto> cmbProduto;
    private javax.swing.JComboBox<String> cmbStatus;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblData;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JSpinner spnQuantidade;
    private javax.swing.JTable tabelaItens;
    // End of variables declaration//GEN-END:variables
}
