package com.lanchonete.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe responsavel por centralizar a conexao com o banco de dados MySQL.
 *
 * IMPORTANTE: ajuste as constantes USUARIO e SENHA conforme a configuracao
 * do seu MySQL (XAMPP/WAMP). Por padrao, o XAMPP usa usuario "root" e senha
 * em branco.
 *
 * Se voce estiver usando o MySQL Connector/J na versao 8.x, o driver abaixo
 * (com.mysql.cj.jdbc.Driver) funciona normalmente. Caso esteja usando uma
 * versao antiga (5.1.x), troque para "com.mysql.jdbc.Driver" e remova o
 * parametro "serverTimezone" da URL, se necessario.
 */
public class ConexaoBD {

    private static final String URL =
            "jdbc:mysql://localhost:3306/lanchonete_delivery?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USUARIO = "root";
    private static final String SENHA = "";
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    public static Connection getConexao() throws SQLException {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver JDBC do MySQL nao encontrado no "
                    + "classpath. Adicione o mysql-connector-java.jar as "
                    + "bibliotecas do projeto.", e);
        }
        return DriverManager.getConnection(URL, USUARIO, SENHA);
    }

    /**
     * Fecha a conexao silenciosamente, ignorando possiveis erros.
     * Utilizado em blocos finally.
     */
    public static void fechar(Connection conexao) {
        if (conexao != null) {
            try {
                conexao.close();
            } catch (SQLException e) {
                System.err.println("Erro ao fechar conexao: " + e.getMessage());
            }
        }
    }
}
