package br.ucb.agendaucb.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import br.ucb.agendaucb.model.Administrativo;
import br.ucb.agendaucb.model.Aluno;
import br.ucb.agendaucb.model.Professor;
import br.ucb.agendaucb.model.Usuario;
import br.ucb.agendaucb.model.enums.TipoUsuario;
import br.ucb.agendaucb.util.connectionBD;

public class UsuarioDAO {

    public void cadastrar(Usuario u) throws SQLException {
        cadastrar(u, null, null);
    }

    public void cadastrar(Usuario u, String numeroTelefone, String tipoTelefone) throws SQLException {
        String sqlUsuario = "INSERT INTO usuario (cpf, nome, sobrenome, email_institucional, senha_hash, data_nascimento, tipo_usuario, id_departamento) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = connectionBD.getConexao();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, u.getCpf());
                ps.setString(2, u.getNome());
                ps.setString(3, u.getSobrenome());
                ps.setString(4, u.getEmail_institucional());
                ps.setString(5, u.getSenha_hash());
                ps.setDate(6, u.getData_nascimento());
                ps.setString(7, u.getTipo_usuario().name());
                ps.setInt(8, u.getId_departamento());
                
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        u.setId_usuario(rs.getInt(1));
                    } else {
                        throw new SQLException("Falha ao obter o ID gerado para o usuário.");
                    }
                }
            }

            if (u instanceof Aluno) {
                Aluno a = (Aluno) u;
                String sqlAluno = "INSERT INTO aluno (id_usuario, matricula, curso, semestre_atual, situacao_academica) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement psAluno = conn.prepareStatement(sqlAluno)) {
                    psAluno.setInt(1, u.getId_usuario());
                    psAluno.setString(2, a.getMatricula());
                    psAluno.setString(3, a.getCurso());
                    psAluno.setInt(4, a.getSemestre_atual());
                    psAluno.setString(5, a.getSituacaoAcademica() != null ? a.getSituacaoAcademica().name() : "ATIVO");
                    psAluno.executeUpdate();
                }
            } else if (u instanceof Professor) {
                Professor p = (Professor) u;
                String sqlProf = "INSERT INTO professor (id_usuario, matricula_docente, titulacao, regime_trabalho) VALUES (?, ?, ?, ?)";
                try (PreparedStatement psProf = conn.prepareStatement(sqlProf)) {
                    psProf.setInt(1, u.getId_usuario());
                    psProf.setString(2, p.getMatriculaDocente());
                    psProf.setString(3, p.getTitulacao());
                    psProf.setString(4, p.getRegimeTrabalho());
                    psProf.executeUpdate();
                }
            } else if (u instanceof Administrativo) {
                Administrativo adm = (Administrativo) u;
                String sqlAdm = "INSERT INTO administrativo (id_usuario, cargo, setor, ramal) VALUES (?, ?, ?, ?)";
                try (PreparedStatement psAdm = conn.prepareStatement(sqlAdm)) {
                    psAdm.setInt(1, u.getId_usuario());
                    psAdm.setString(2, adm.getCargo());
                    psAdm.setString(3, adm.getSetor());
                    psAdm.setString(4, adm.getRamal());
                    psAdm.executeUpdate();
                }
            }

            if (numeroTelefone != null && !numeroTelefone.trim().isEmpty()) {
                String sqlTel = "INSERT INTO telefone_usuario (id_usuario, numero_telefone, tipo_telefone) VALUES (?, ?, ?)";
                try (PreparedStatement psTel = conn.prepareStatement(sqlTel)) {
                    psTel.setInt(1, u.getId_usuario());
                    psTel.setString(2, numeroTelefone);
                    psTel.setString(3, tipoTelefone != null ? tipoTelefone : "CELULAR");
                    psTel.executeUpdate();
                }
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
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
        String query = "SELECT * FROM usuario WHERE id_usuario = ?";
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
        String query = "UPDATE usuario SET cpf = ?, nome = ?, sobrenome = ?, email_institucional = ?, senha_hash = ?, data_nascimento = ?, tipo_usuario = ?, id_departamento = ? WHERE id_usuario = ?";

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
        String query = "DELETE FROM usuario WHERE id_usuario = ?";

        try (Connection conn = connectionBD.getConexao();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, idUsuario);
            ps.executeUpdate();
        }
    }

    private Usuario instanciarUsuario(ResultSet rs) throws SQLException {
        return new Usuario(
                rs.getInt("id_usuario"),
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