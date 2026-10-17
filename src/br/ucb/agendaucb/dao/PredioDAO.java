package br.ucb.agendaucb.dao;

import br.ucb.agendaucb.util.connectionBD; 
import br.ucb.agendaucb.model.Predio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PredioDAO {

    public void cadastrar(Predio p) throws SQLException {
        String query = "INSERT INTO predio (nome, logradouro, cep, quantidade_andares) VALUES (?, ?, ?, ?)";

        try (Connection conn = connectionBD.getConexao();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, p.getNome());
            ps.setString(2, p.getLogradouro());
            ps.setString(3, p.getCep());
            ps.setInt(4, p.getQntd_andares());

            ps.executeUpdate();
        }
    }

    public List<Predio> listarTudo() throws SQLException {
        List<Predio> list = new ArrayList<>();
        String query = "SELECT * FROM predio";

        try (Connection conn = connectionBD.getConexao();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Predio p = new Predio(
                        rs.getInt("id_predio"),
                        rs.getString("nome"),
                        rs.getString("logradouro"),
                        rs.getString("cep"),
                        rs.getInt("quantidade_andares")
                );
                list.add(p);
            }
        }
        return list;
    }

    public Predio buscarPorId(int id) throws SQLException {
        String query = "SELECT * FROM predio WHERE id_predio = ?";
        Predio p = null; // Declarado fora para evitar erro de escopo

        try (Connection conn = connectionBD.getConexao();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    p = new Predio(
                            rs.getInt("id_predio"),
                            rs.getString("nome"),
                            rs.getString("logradouro"),
                            rs.getString("cep"),
                            rs.getInt("quantidade_andares")
                    );
                }
            }
        }
        return p;
    }

    public void atualizar(Predio p) throws SQLException {
        String query = "UPDATE predio SET nome = ?, logradouro = ?, cep = ?, quantidade_andares = ? WHERE id_predio = ?";

        try (Connection conn = connectionBD.getConexao();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, p.getNome());
            ps.setString(2, p.getLogradouro());
            ps.setString(3, p.getCep());
            ps.setInt(4, p.getQntd_andares());
            ps.setInt(5, p.getId_predio());

            ps.executeUpdate();
        }
    }

    public void deletar(int id) throws SQLException {
        String query = "DELETE FROM predio WHERE id_predio = ?";

        try (Connection conn = connectionBD.getConexao();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}