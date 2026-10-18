package br.ucb.agendaucb.model;

import java.sql.Date;
import br.ucb.agendaucb.model.enums.TipoUsuario;

public class Professor extends Usuario {

    private String matricula_docente;
    private String titulacao;
    private String regimeTrabalho;

    public Professor() {
        super();
    }

    public Professor(int id_usuario, String matricula_docente, String titulacao, String regimeTrabalho) {
        this.setId_usuario(id_usuario);
        this.matricula_docente = matricula_docente;
        this.titulacao = titulacao;
        this.regimeTrabalho = regimeTrabalho;
    }

    public Professor(int id_usuario, String cpf, String nome, String sobrenome, String email_institucional,
            String senha_hash, Date data_nascimento, TipoUsuario tipo_usuario, int id_departamento,
            String matricula_docente, String titulacao, String regimeTrabalho) {
        super(id_usuario, cpf, nome, sobrenome, email_institucional, senha_hash, data_nascimento, tipo_usuario,
                id_departamento);
        this.matricula_docente = matricula_docente;
        this.titulacao = titulacao;
        this.regimeTrabalho = regimeTrabalho;
    }

    public String getMatriculaDocente() {
        return matricula_docente;
    }

    public void setMatricula_docente(String matricula_docente) {
        this.matricula_docente = matricula_docente;
    }

    public String getTitulacao() {
        return titulacao;
    }

    public void setTitulacao(String titulacao) {
        this.titulacao = titulacao;
    }

    public String getRegimeTrabalho() {
        return regimeTrabalho;
    }

    public void setRegimeTrabalho(String regimeTrabalho) {
        this.regimeTrabalho = regimeTrabalho;
    }
}