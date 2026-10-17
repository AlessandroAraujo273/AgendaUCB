package br.ucb.agendaucb.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.ucb.agendaucb.model.Recurso;
import br.ucb.agendaucb.model.enums.TipoRecurso;
import br.ucb.agendaucb.util.connectionBD;

public class RecursoDAO {
    public void cadastrar(Recurso r) throws SQLException {
        String query = "INSERT INTO recurso (nome, tipo_recurso, patrimonio, quantidade_total_disponivel) VALUES (?, ?, ?, ?)";

        try (Connection conn = connectionBD.getConexao();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, r.getNome());
            ps.setString(2, r.getTipo_recurso().name());
            ps.setString(3, r.getPatrimonio());
            ps.setInt(4, r.getQntd_disponivel());

            ps.executeUpdate();
        }
    }

    public List<Recurso> listarTudo() throws SQLException {
        String query = "SELECT * FROM recurso";
        List<Recurso> list = new ArrayList<>();

        try (Connection conn = connectionBD.getConexao();
                PreparedStatement ps = conn.prepareStatement(query);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Recurso r = new Recurso(
                        rs.getInt("id_recurso"),
                        rs.getString("nome"),
                        TipoRecurso.valueOf(rs.getString("tipo_recurso")),
                        rs.getString("patrimonio"),
                        rs.getInt("quantidade_total_disponivel"));
                list.add(r);
            }
        }
        return list;
    }

    public Recurso buscarPorId(int idRecurso) throws SQLException {
        String query = "SELECT * FROM recurso WHERE id_recurso = ?";
        Recurso r = null;

        try (Connection conn = connectionBD.getConexao();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, idRecurso);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    r = new Recurso(
                            rs.getInt("id_recurso"),
                            rs.getString("nome"),
                            TipoRecurso.valueOf(rs.getString("tipo_recurso")),
                            rs.getString("patrimonio"),
                            rs.getInt("quantidade_total_disponivel"));
                }
            }
        }
        return r;
    }

    public void atualizar(Recurso r) throws SQLException {
        String query = "UPDATE recurso SET nome = ?, tipo_recurso = ?, patrimonio = ?, quantidade_total_disponivel = ? WHERE id_recurso = ?";

        try (Connection conn = connectionBD.getConexao();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, r.getNome());
            ps.setString(2, r.getTipo_recurso().name());
            ps.setString(3, r.getPatrimonio());
            ps.setInt(4, r.getQntd_disponivel());
            ps.setInt(5, r.getId_recurso());

            ps.executeUpdate();
        }
    }

    public void deletar(int idRecurso) throws SQLException {
        String query = "DELETE FROM recurso WHERE id_recurso = ?";

        try (Connection conn = connectionBD.getConexao();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, idRecurso);
            ps.executeUpdate();
        }
    }
}