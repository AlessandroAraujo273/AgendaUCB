package br.ucb.agendaucb.model;

import java.sql.Date;
import br.ucb.agendaucb.model.enums.TipoUsuario;

public class Usuario {
    private int id_usuario;
    private String cpf;
    private String nome;
    private String sobrenome;
    private String email_institucional;
    private String senha_hash;
    private Date data_nascimento;
    private TipoUsuario tipo_usuario;
    private int id_departamento;

    public Usuario() {
    }

    public Usuario(int id_usuario, String cpf, String nome, String sobrenome, String email_institucional,
            String senha_hash, Date data_nascimento, TipoUsuario tipo_usuario, int id_departamento) {
        this.id_usuario = id_usuario;
        this.cpf = cpf;
        this.nome = nome;
        this.sobrenome = sobrenome;
        this.email_institucional = email_institucional;
        this.senha_hash = senha_hash;
        this.data_nascimento = data_nascimento;
        this.tipo_usuario = tipo_usuario;
        this.id_departamento = id_departamento;
    }

    public int getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(int id_usuario) {
        this.id_usuario = id_usuario;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSobrenome() {
        return sobrenome;
    }

    public void setSobrenome(String sobrenome) {
        this.sobrenome = sobrenome;
    }

    public String getEmail_institucional() {
        return email_institucional;
    }

    public void setEmail_institucional(String email_institucional) {
        this.email_institucional = email_institucional;
    }

    public String getSenha_hash() {
        return senha_hash;
    }

    public void setSenha_hash(String senha_hash) {
        this.senha_hash = senha_hash;
    }

    public Date getData_nascimento() {
        return data_nascimento;
    }

    public void setData_nascimento(Date data_nascimento) {
        this.data_nascimento = data_nascimento;
    }

    public TipoUsuario getTipo_usuario() {
        return tipo_usuario;
    }

    public void setTipo_usuario(TipoUsuario tipo_usuario) {
        this.tipo_usuario = tipo_usuario;
    }

    public int getId_departamento() {
        return id_departamento;
    }

    public void setId_departamento(int id_departamento) {
        this.id_departamento = id_departamento;
    }
}