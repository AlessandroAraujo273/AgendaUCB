package br.ucb.agendaucb.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.ucb.agendaucb.model.Usuario;
import br.ucb.agendaucb.model.enums.TipoUsuario;
import br.ucb.agendaucb.util.connectionBD;

public class UsuarioDAO {

    public void cadastrar(Usuario u) throws SQLException {
        String query = "INSERT INTO usuario (cpf, nome, sobrenome, email_institucional, senha_hash, data_nascimento, tipo_usuario, id_departamento) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = connectionBD.getConexao();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, u.getCpf());
            ps.setString(2, u.getNome());
            ps.setString(3, u.getSobrenome());
            ps.setString(4, u.getEmail_institucional());
            ps.setString(5, u.getSenha_hash());
            ps.setDate(6, u.getData_nascimento());
            ps.setString(7, u.getTipo_usuario().name());
            ps.setInt(8, u.getId_departamento());

            ps.executeUpdate();
        }
    }

    public Usuario autenticar(String emailInstitucional, String senhaHash) throws SQLException {
        String query = "SELECT * FROM usuario WHERE email_institucional = ? AND senha_hash = ?";
        Usuario u = null;

        try (Connection conn = connectionBD.getConexao();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, emailInstitucional);
            ps.setString(2, senhaHash);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    u = instanciarUsuario(rs);
                }
            }
        }
        return u;
    }

    public List<Usuario> listarTudo() throws SQLException {
        String query = "SELECT * FROM usuario";
        List<Usuario> list = new ArrayList<>();

        try (Connection conn = connectionBD.getConexao();
                PreparedStatement ps = conn.prepareStatement(query);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(instanciarUsuario(rs));
            }
        }
        return list;
    }

    public Usuario buscarPorId(int idUsuario) throws SQLException {
        String query = "SELECT * FROM usuario WHERE Id_usuario = ?";
        Usuario u = null;

        try (Connection conn = connectionBD.getConexao();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    u = instanciarUsuario(rs);
                }
            }
        }
        return u;
    }

    public void atualizar(Usuario u) throws SQLException {
        String query = "UPDATE usuario SET cpf = ?, nome = ?, sobrenome = ?, email_institucional = ?, senha_hash = ?, data_nascimento = ?, tipo_usuario = ?, id_departamento = ? WHERE Id_usuario = ?";

        try (Connection conn = connectionBD.getConexao();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, u.getCpf());
            ps.setString(2, u.getNome());
            ps.setString(3, u.getSobrenome());
            ps.setString(4, u.getEmail_institucional());
            ps.setString(5, u.getSenha_hash());
            ps.setDate(6, u.getData_nascimento());
            ps.setString(7, u.getTipo_usuario().name());
            ps.setInt(8, u.getId_departamento());
            ps.setInt(9, u.getId_usuario());

            ps.executeUpdate();
        }
    }

    public void deletar(int idUsuario) throws SQLException {
        String query = "DELETE FROM usuario WHERE Id_usuario = ?";

        try (Connection conn = connectionBD.getConexao();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, idUsuario);
            ps.executeUpdate();
        }
    }

    private Usuario instanciarUsuario(ResultSet rs) throws SQLException {
        return new Usuario(
                rs.getInt("Id_usuario"),
                rs.getString("cpf"),
                rs.getString("nome"),
                rs.getString("sobrenome"),
                rs.getString("email_institucional"),
                rs.getString("senha_hash"),
                rs.getDate("data_nascimento"),
                TipoUsuario.valueOf(rs.getString("tipo_usuario")),
                rs.getInt("id_departamento"));
    }
}