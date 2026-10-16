package br.ucb.agendaucb;

import br.ucb.agendaucb.util.connectionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Teste de Conexão: AgendaUCB ===");

        try (Connection conn = connectionBD.getConexao()) {
            System.out.println("✅ Conexão com o MySQL realizada com sucesso!");

            // Testa a leitura dos dados carregados pelo script 02_carga.sql
            String sql = "SELECT id_predio, nome, quantidade_andares FROM predio";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                System.out.println("\n--- Prédios Encontrados no Banco ---");
                while (rs.next()) {
                    int id = rs.getInt("id_predio");
                    String nome = rs.getString("nome");
                    int andares = rs.getInt("quantidade_andares");
                    System.out.println("ID: " + id + " | Prédio: " + nome + " | Andares: " + andares);
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Erro ao conectar ou executar a consulta no MySQL:");
            e.printStackTrace();
        }
    }
}