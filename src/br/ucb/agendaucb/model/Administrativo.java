package br.ucb.agendaucb.model;

import java.sql.Date;

import br.ucb.agendaucb.model.enums.TipoUsuario;

public class Administrativo extends Usuario {

    private String cargo;
    private String setor;
    private String ramal;

    public Administrativo() {
    };

    public Administrativo(int id_usuario, String cpf, String nome, String sobrenome, String email_institucional,
            String senha_hash, Date data_nascimento, TipoUsuario tipo_usuario, int id_departamento, String cargo,
            String setor, String ramal) {
        super(id_usuario, cpf, nome, sobrenome, email_institucional, senha_hash, data_nascimento, tipo_usuario,
                id_departamento);
        this.cargo = cargo;
        this.setor = setor;
        this.ramal = ramal;
    }

    public Administrativo(String cargo, String setor, String ramal) {
        this.cargo = cargo;
        this.setor = setor;
        this.ramal = ramal;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getSetor() {
        return setor;
    }

    public void setSetor(String setor) {
        this.setor = setor;
    }

    public String getRamal() {
        return ramal;
    }

    public void setRamal(String ramal) {
        this.ramal = ramal;
    }

}
