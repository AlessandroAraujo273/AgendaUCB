package br.ucb.agendaucb.model;

import java.sql.Date;
import br.ucb.agendaucb.model.enums.SituacaoAcademica;
import br.ucb.agendaucb.model.enums.TipoUsuario;

public class Aluno extends Usuario {

    private String matricula;
    private String curso;
    private int semestre_atual;
    private SituacaoAcademica situacaoAcademica;

    public Aluno() {
        super();
    }

    public Aluno(int id_usuario, String matricula, String curso, int semestre_atual,
            SituacaoAcademica situacaoAcademica) {
        this.setId_usuario(id_usuario);
        this.matricula = matricula;
        this.curso = curso;
        this.semestre_atual = semestre_atual;
        this.situacaoAcademica = situacaoAcademica;
    }

    public Aluno(int id_usuario, String cpf, String nome, String sobrenome, String email_institucional,
            String senha_hash, Date data_nascimento, TipoUsuario tipo_usuario, int id_departamento,
            String matricula, String curso, int semestre_atual, SituacaoAcademica situacaoAcademica) {
        super(id_usuario, cpf, nome, sobrenome, email_institucional, senha_hash, data_nascimento, tipo_usuario,
                id_departamento);
        this.matricula = matricula;
        this.curso = curso;
        this.semestre_atual = semestre_atual;
        this.situacaoAcademica = situacaoAcademica;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public int getSemestre_atual() {
        return semestre_atual;
    }

    public void setSemestre_atual(int semestre_atual) {
        this.semestre_atual = semestre_atual;
    }

    public SituacaoAcademica getSituacaoAcademica() {
        return situacaoAcademica;
    }

    public void setSituacaoAcademica(SituacaoAcademica situacaoAcademica) {
        this.situacaoAcademica = situacaoAcademica;
    }
}