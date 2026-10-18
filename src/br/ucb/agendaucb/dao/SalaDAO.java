package br.ucb.agendaucb.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.ucb.agendaucb.model.Sala;
import br.ucb.agendaucb.model.enums.TipoSala;
import br.ucb.agendaucb.util.connectionBD;

public class SalaDAO {

    public void cadastrar(Sala s) throws SQLException {
        String query = "INSERT INTO sala (id_predio, numero_sala, capacidade, tipo_sala, possui_projetor) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = connectionBD.getConexao();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, s.getId_predio());
            ps.setString(2, s.getNumero_sala());
            ps.setInt(3, s.getCapacidade());
            ps.setString(4, s.getTipo_sala().name());
            ps.setBoolean(5, s.isPossui_projetor());

            ps.executeUpdate();
        }
    }

    public List<Sala> listarTudo() throws SQLException {
        String query = "SELECT * FROM sala";
        List<Sala> list = new ArrayList<>();

        try (Connection conn = connectionBD.getConexao();
                PreparedStatement ps = conn.prepareStatement(query);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Sala s = new Sala(
                        rs.getInt("id_predio"),
                        rs.getString("numero_sala"),
                        rs.getInt("capacidade"),
                        TipoSala.valueOf(rs.getString("tipo_sala")),
                        rs.getBoolean("possui_projetor"));
                list.add(s);
            }
        }
        return list;
    }

    public Sala buscarPorChave(int idPredio, String numeroSala) throws SQLException {
        String query = "SELECT * FROM sala WHERE id_predio = ? AND numero_sala = ?";
        Sala s = null;

        try (Connection conn = connectionBD.getConexao();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, idPredio);
            ps.setString(2, numeroSala);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    s = new Sala(
                            rs.getInt("id_predio"),
                            rs.getString("numero_sala"),
                            rs.getInt("capacidade"),
                            TipoSala.valueOf(rs.getString("tipo_sala")),
                            rs.getBoolean("possui_projetor"));
                }
            }
        }
        return s;
    }

    public List<Sala> listarPorPredio(int idPredio) throws SQLException {
        List<Sala> lista = new ArrayList<>();
        String query = "SELECT * FROM sala WHERE id_predio = ?";

        try (Connection conn = connectionBD.getConexao();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, idPredio);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Sala s = new Sala(
                            rs.getInt("id_predio"),
                            rs.getString("numero_sala"),
                            rs.getInt("capacidade"),
                            TipoSala.valueOf(rs.getString("tipo_sala")),
                            rs.getBoolean("possui_projetor"));
                    lista.add(s);
                }
            }
        }
        return lista;
    }

    public void atualizar(Sala s) throws SQLException {
        String query = "UPDATE sala SET capacidade = ?, tipo_sala = ?, possui_projetor = ? WHERE id_predio = ? AND numero_sala = ?";

        try (Connection conn = connectionBD.getConexao();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, s.getCapacidade());
            ps.setString(2, s.getTipo_sala().name());
            ps.setBoolean(3, s.isPossui_projetor());

            ps.setInt(4, s.getId_predio());
            ps.setString(5, s.getNumero_sala());

            ps.executeUpdate();
        }
    }

    public void deletar(int idPredio, String numeroSala) throws SQLException {
        String query = "DELETE FROM sala WHERE id_predio = ? AND numero_sala = ?";

        try (Connection conn = connectionBD.getConexao();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, idPredio);
            ps.setString(2, numeroSala);

            ps.executeUpdate();
        }
    }
}