package com.lanchonete;

import com.lanchonete.view.TelaPrincipal;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Classe principal: ponto de entrada da aplicacao.
 */
public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Se nao conseguir aplicar o look and feel do sistema, segue com o padrao.
            System.err.println("Nao foi possivel aplicar o look and feel do sistema: " + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            TelaPrincipal tela = new TelaPrincipal();
            tela.setVisible(true);
        });
    }
}
