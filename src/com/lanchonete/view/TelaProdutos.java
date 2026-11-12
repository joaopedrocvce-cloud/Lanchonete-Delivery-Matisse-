/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lanchonete.view;

import com.lanchonete.dao.ProdutoDAO;
import com.lanchonete.model.Produto;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author aluno
 */
public class TelaProdutos extends javax.swing.JFrame {

    /**
     * Creates new form TelaProdutos
     */
        private static final String PASTA_IMAGENS = "imagens_produtos";

    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private DefaultTableModel modeloTabela;
    private Integer idSelecionado = null;
    private String caminhoImagemAtual = null;

    public TelaProdutos() {
        initComponents();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        String[] colunas = {"ID", "Nome", "Descricao", "Preco", "Categoria"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabela.setModel(modeloTabela);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabela.getSelectedRow() != -1) {
                preencherFormularioComLinhaSelecionada();
            }
        });

        atualizarPreviaImagem(null);
        carregarProdutos();
    }

    private void escolherImagem() {
        JFileChooser seletor = new JFileChooser();
        seletor.setFileFilter(new FileNameExtensionFilter(
                "Imagens (*.png, *.jpg, *.jpeg)", "png", "jpg", "jpeg"));

        int resultado = seletor.showOpenDialog(this);
        if (resultado != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File arquivoOrigem = seletor.getSelectedFile();
        try {
            Path pastaDestino = Paths.get(PASTA_IMAGENS);
            if (!Files.exists(pastaDestino)) {
                Files.createDirectories(pastaDestino);
            }

            String nomeArquivoDestino = System.currentTimeMillis() + "_" + arquivoOrigem.getName();
            Path caminhoDestino = pastaDestino.resolve(nomeArquivoDestino);
            Files.copy(arquivoOrigem.toPath(), caminhoDestino, StandardCopyOption.REPLACE_EXISTING);

            caminhoImagemAtual = caminhoDestino.toString();
            atualizarPreviaImagem(caminhoImagemAtual);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Nao foi possivel copiar a imagem selecionada:\n" + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void atualizarPreviaImagem(String caminho) {
        if (caminho == null || caminho.trim().isEmpty()) {
            lblImagem.setIcon(null);
            lblImagem.setText("(sem imagem)");
            return;
        }
        File arquivo = new File(caminho);
        if (!arquivo.exists()) {
            lblImagem.setIcon(null);
            lblImagem.setText("(imagem nao encontrada)");
            return;
        }
        ImageIcon icone = new ImageIcon(caminho);
        java.awt.Image imagemEscalada = icone.getImage().getScaledInstance(76, 76, java.awt.Image.SCALE_SMOOTH);
        lblImagem.setIcon(new ImageIcon(imagemEscalada));
        lblImagem.setText(null);
    }

    private void carregarProdutos() {
        try {
            modeloTabela.setRowCount(0);
            List<Produto> produtos = produtoDAO.listarTodos();
            for (Produto p : produtos) {
                modeloTabela.addRow(new Object[]{
                    p.getId(), p.getNome(), p.getDescricao(), "R$ " + p.getPreco(), p.getCategoria()
                });
            }
        } catch (SQLException e) {
            exibirErroBanco(e);
        }
    }

    private void preencherFormularioComLinhaSelecionada() {
        int linha = tabela.getSelectedRow();
        idSelecionado = (Integer) modeloTabela.getValueAt(linha, 0);
        try {
            Produto produto = produtoDAO.buscarPorId(idSelecionado);
            if (produto != null) {
                txtNome.setText(produto.getNome());
                txtDescricao.setText(produto.getDescricao() == null ? "" : produto.getDescricao());
                txtPreco.setText(produto.getPreco().toString());
                txtCategoria.setText(produto.getCategoria() == null ? "" : produto.getCategoria());
                caminhoImagemAtual = produto.getImagemCaminho();
                atualizarPreviaImagem(caminhoImagemAtual);
            }
        } catch (SQLException e) {
            exibirErroBanco(e);
        }
    }

    private void limparFormulario() {
        idSelecionado = null;
        caminhoImagemAtual = null;
        txtNome.setText("");
        txtDescricao.setText("");
        txtPreco.setText("");
        txtCategoria.setText("");
        atualizarPreviaImagem(null);
        tabela.clearSelection();
    }

    private void salvar() {
        if (txtNome.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "O campo Nome e obrigatorio.",
                    "Validacao", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal preco;
        try {
            preco = new BigDecimal(txtPreco.getText().trim().replace(",", "."));
            if (preco.compareTo(BigDecimal.ZERO) < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Informe um preco valido (ex.: 19.90).", "Validacao", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Produto produto = new Produto();
        produto.setNome(txtNome.getText().trim());
        produto.setDescricao(txtDescricao.getText().trim());
        produto.setPreco(preco);
        produto.setCategoria(txtCategoria.getText().trim());
        produto.setImagemCaminho(caminhoImagemAtual);

        try {
            if (idSelecionado == null) {
                produtoDAO.inserir(produto);
                JOptionPane.showMessageDialog(this, "Produto cadastrado com sucesso!");
            } else {
                produto.setId(idSelecionado);
                produtoDAO.atualizar(produto);
                JOptionPane.showMessageDialog(this, "Produto atualizado com sucesso!");
            }
            limparFormulario();
            carregarProdutos();
        } catch (SQLException e) {
            exibirErroBanco(e);
        }
    }

    private void excluir() {
        if (idSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um produto na tabela para excluir.",
                    "Atencao", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirmacao = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja excluir este produto?", "Confirmar exclusao",
                JOptionPane.YES_NO_OPTION);
        if (confirmacao != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            if (produtoDAO.estaEmUso(idSelecionado)) {
                JOptionPane.showMessageDialog(this,
                        "Este produto ja foi usado em pedidos e nao pode ser excluido.",
                        "Exclusao bloqueada", JOptionPane.ERROR_MESSAGE);
                return;
            }
            produtoDAO.excluir(idSelecionado);
            JOptionPane.showMessageDialog(this, "Produto excluido com sucesso!");
            limparFormulario();
            carregarProdutos();
        } catch (SQLException e) {
            exibirErroBanco(e);
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
        jLabel5 = new javax.swing.JLabel();
        txtNome = new javax.swing.JTextField();
        txtPreco = new javax.swing.JTextField();
        txtDescricao = new javax.swing.JTextField();
        txtCategoria = new javax.swing.JTextField();
        lblImagem = new javax.swing.JLabel();
        btnEscolherImagem = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabela = new javax.swing.JTable();
        btnSalvar = new javax.swing.JButton();
        btnExcluir = new javax.swing.JButton();
        btnLimpar = new javax.swing.JButton();
        btnFechar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Nome");

        jLabel2.setText("Preco (R$)");

        jLabel3.setText("Descricao");

        jLabel4.setText("Categoria");

        jLabel5.setText("Imagem");

        lblImagem.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblImagem.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        lblImagem.setPreferredSize(new java.awt.Dimension(80, 80));

        btnEscolherImagem.setText("Escolher Imagem");
        btnEscolherImagem.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnEscolherImagem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEscolherImagemActionPerformed(evt);
            }
        });

        tabela.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tabela);

        btnSalvar.setText("Salvar");
        btnSalvar.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnSalvar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSalvarActionPerformed(evt);
            }
        });

        btnExcluir.setText("Excluir");
        btnExcluir.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnExcluir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExcluirActionPerformed(evt);
            }
        });

        btnLimpar.setText("Limpar");
        btnLimpar.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnLimpar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimparActionPerformed(evt);
            }
        });

        btnFechar.setText("Fechar");
        btnFechar.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
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
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(44, 44, 44)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel1)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel3)))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(28, 28, 28)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(btnExcluir)
                                    .addComponent(btnSalvar, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnEscolherImagem, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(60, 60, 60)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btnLimpar)
                                    .addComponent(btnFechar))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lblImagem, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(155, 155, 155)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtCategoria)
                                    .addComponent(txtPreco)
                                    .addComponent(txtDescricao)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(txtNome, javax.swing.GroupLayout.PREFERRED_SIZE, 251, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE)))))
                        .addGap(67, 67, 67)))
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 536, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnExcluir, btnFechar, btnLimpar, btnSalvar});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(txtNome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(txtPreco, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(txtDescricao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(txtCategoria, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(17, 17, 17)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(26, 26, 26)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel5)
                                    .addComponent(btnEscolherImagem)))
                            .addComponent(lblImagem, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(26, 26, 26)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnSalvar)
                            .addComponent(btnLimpar))
                        .addGap(53, 53, 53)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnExcluir)
                            .addComponent(btnFechar))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnEscolherImagemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEscolherImagemActionPerformed
        escolherImagem();
    }//GEN-LAST:event_btnEscolherImagemActionPerformed

    private void btnSalvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSalvarActionPerformed
        salvar();
    }//GEN-LAST:event_btnSalvarActionPerformed

    private void btnExcluirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExcluirActionPerformed
        excluir();
    }//GEN-LAST:event_btnExcluirActionPerformed

    private void btnLimparActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimparActionPerformed
        limparFormulario();
    }//GEN-LAST:event_btnLimparActionPerformed

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
            java.util.logging.Logger.getLogger(TelaProdutos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TelaProdutos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TelaProdutos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TelaProdutos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TelaProdutos().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEscolherImagem;
    private javax.swing.JButton btnExcluir;
    private javax.swing.JButton btnFechar;
    private javax.swing.JButton btnLimpar;
    private javax.swing.JButton btnSalvar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblImagem;
    private javax.swing.JTable tabela;
    private javax.swing.JTextField txtCategoria;
    private javax.swing.JTextField txtDescricao;
    private javax.swing.JTextField txtNome;
    private javax.swing.JTextField txtPreco;
    // End of variables declaration//GEN-END:variables
}
