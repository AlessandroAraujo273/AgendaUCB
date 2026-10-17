package br.ucb.agendaucb.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import br.ucb.agendaucb.model.Agendamento;
import br.ucb.agendaucb.model.enums.SituacaoAgendamento;
import br.ucb.agendaucb.util.connectionBD;

public class AgendamentoDAO {

    public boolean temConflitoHorario(int idPredio, String numeroSala, Timestamp inicio, Timestamp fim)
            throws SQLException {
        String query = "SELECT COUNT(*) FROM agendamento " +
                "WHERE id_predio = ? AND numero_sala = ? AND situacao_atual != 'CANCELADO' " +
                "AND ((data_hora_inicio < ? AND data_hora_fim > ?))";

        try (Connection conn = connectionBD.getConexao();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, idPredio);
            ps.setString(2, numeroSala);
            ps.setTimestamp(3, fim);
            ps.setTimestamp(4, inicio);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public boolean cadastrar(Agendamento a) throws SQLException {
        if (temConflitoHorario(a.getIdPredio(), a.getNumeroSala(), a.getInicio(), a.getFim())) {
            return false;
        }

        String query = "INSERT INTO agendamento (data_hora_inicio, data_hora_fim, situacao_atual, motivo_descricao, data_criacao, id_usuario_solicitante, id_predio, numero_sala, id_tipo) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = connectionBD.getConexao();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setTimestamp(1, a.getInicio());
            ps.setTimestamp(2, a.getFim());
            ps.setString(3, a.getSituacao().name());
            ps.setString(4, a.getMotivo());
            ps.setTimestamp(5,
                    a.getDataCriacao() != null ? a.getDataCriacao() : new Timestamp(System.currentTimeMillis()));
            ps.setInt(6, a.getIdUsuario());
            ps.setInt(7, a.getIdPredio());
            ps.setString(8, a.getNumeroSala());
            ps.setInt(9, a.getIdTipo());

            ps.executeUpdate();
            return true;
        }
    }

    public List<Agendamento> listarTudo() throws SQLException {
        String query = "SELECT * FROM agendamento";
        List<Agendamento> list = new ArrayList<>();

        try (Connection conn = connectionBD.getConexao();
                PreparedStatement ps = conn.prepareStatement(query);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(instanciarAgendamento(rs));
            }
        }
        return list;
    }

    public Agendamento buscarPorId(int idAgendamento) throws SQLException {
        String query = "SELECT * FROM agendamento WHERE Id_agendamento = ?";
        Agendamento a = null;

        try (Connection conn = connectionBD.getConexao();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, idAgendamento);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    a = instanciarAgendamento(rs);
                }
            }
        }
        return a;
    }

    public void atualizar(Agendamento a) throws SQLException {
        String query = "UPDATE agendamento SET data_hora_inicio = ?, data_hora_fim = ?, situacao_atual = ?, motivo_descricao = ?, "
                +
                "data_criacao = ?, id_usuario_solicitante = ?, id_predio = ?, numero_sala = ?, id_tipo = ? WHERE Id_agendamento = ?";

        try (Connection conn = connectionBD.getConexao();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setTimestamp(1, a.getInicio());
            ps.setTimestamp(2, a.getFim());
            ps.setString(3, a.getSituacao().name());
            ps.setString(4, a.getMotivo());
            ps.setTimestamp(5, a.getDataCriacao());
            ps.setInt(6, a.getIdUsuario());
            ps.setInt(7, a.getIdPredio());
            ps.setString(8, a.getNumeroSala());
            ps.setInt(9, a.getIdTipo());
            ps.setInt(10, a.getIdAgendamento());

            ps.executeUpdate();
        }
    }

    public void deletar(int idAgendamento) throws SQLException {
        String query = "DELETE FROM agendamento WHERE Id_agendamento = ?";

        try (Connection conn = connectionBD.getConexao();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, idAgendamento);
            ps.executeUpdate();
        }
    }

    private Agendamento instanciarAgendamento(ResultSet rs) throws SQLException {
        return new Agendamento(
                rs.getInt("Id_agendamento"),
                rs.getTimestamp("data_hora_inicio"),
                rs.getTimestamp("data_hora_fim"),
                SituacaoAgendamento.valueOf(rs.getString("situacao_atual")),
                rs.getString("motivo_descricao"),
                rs.getTimestamp("data_criacao"),
                rs.getInt("id_usuario_solicitante"),
                rs.getInt("id_predio"),
                rs.getString("numero_sala"),
                rs.getInt("id_tipo"));
    }
}