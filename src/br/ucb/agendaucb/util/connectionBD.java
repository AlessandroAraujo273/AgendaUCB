package br.ucb.agendaucb.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class connectionBD {
    private static String url;
    private static String user;
    private static String password;

    static {
        Properties prop = new Properties();
        // Carrega o arquivo db.properties localizado na pasta src
        try (InputStream input = connectionBD.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input != null) {
                prop.load(input);
                url = prop.getProperty("db.url");
                user = prop.getProperty("db.user");
                password = prop.getProperty("db.password");
            } else {
                // Fallback padrão se o arquivo db.properties não for criado
                url = "jdbc:mysql://localhost:3306/agendaucb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
                user = "root";
                password = "root";
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar configurações do banco de dados: " + e.getMessage());
        }
    }

    public static Connection getConexao() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver JDBC do MySQL não encontrado na pasta lib.", e);
        }
    }
}