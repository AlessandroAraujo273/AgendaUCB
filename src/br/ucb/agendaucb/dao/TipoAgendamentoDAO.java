package br.ucb.agendaucb.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.ucb.agendaucb.model.TipoAgendamento;
import br.ucb.agendaucb.util.connectionBD;

public class TipoAgendamentoDAO {

    public void cadastrar(TipoAgendamento t) throws SQLException {
        String query = "INSERT INTO tipo_agendamento (descricao, duracao_padrao_minutos, requer_aprovacao) VALUES (?, ?, ?)";

        try (Connection conn = connectionBD.getConexao();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, t.getDescricao());
            ps.setInt(2, t.getDuracao_minutos());
            ps.setBoolean(3, t.isAprovacao());

            ps.executeUpdate();
        }
    }

    public List<TipoAgendamento> listarTudo() throws SQLException {
        String query = "SELECT * FROM tipo_agendamento";
        List<TipoAgendamento> list = new ArrayList<>();

        try (Connection conn = connectionBD.getConexao();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                TipoAgendamento t = new TipoAgendamento(
                        rs.getInt("id_tipo"),
                        rs.getString("descricao"),
                        rs.getInt("duracao_padrao_minutos"),
                        rs.getBoolean("requer_aprovacao")
                );
                list.add(t);
            }
        }
        return list;
    }

    public TipoAgendamento buscarPorId(int idTipo) throws SQLException {
        String query = "SELECT * FROM tipo_agendamento WHERE id_tipo = ?";
        TipoAgendamento t = null;

        try (Connection conn = connectionBD.getConexao();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, idTipo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    t = new TipoAgendamento(
                            rs.getInt("id_tipo"),
                            rs.getString("descricao"),
                            rs.getInt("duracao_padrao_minutos"),
                            rs.getBoolean("requer_aprovacao")
                    );
                }
            }
        }
        return t;
    }

    public void atualizar(TipoAgendamento t) throws SQLException {
        String query = "UPDATE tipo_agendamento SET descricao = ?, duracao_padrao_minutos = ?, requer_aprovacao = ? WHERE id_tipo = ?";

        try (Connection conn = connectionBD.getConexao();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, t.getDescricao());
            ps.setInt(2, t.getDuracao_minutos());
            ps.setBoolean(3, t.isAprovacao());
            ps.setInt(4, t.getId_tipo());

            ps.executeUpdate();
        }
    }

    public void deletar(int idTipo) throws SQLException {
        String query = "DELETE FROM tipo_agendamento WHERE id_tipo = ?";

        try (Connection conn = connectionBD.getConexao();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, idTipo);
            ps.executeUpdate();
        }
    }
}