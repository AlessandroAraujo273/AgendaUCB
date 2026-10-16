package br.ucb.agendaucb.model;

public class Recurso {
    private int id_recurso;
    private String nome;
    private String tipo_recurso;
    private String patrimonio;
    private int qntd_disponivel;

    public Recurso() {
    };

    public Recurso(int id_recurso, String nome, String tipo_recurso, String patrimonio, int qntd_disponivel) {
        this.id_recurso = id_recurso;
        this.nome = nome;
        this.tipo_recurso = tipo_recurso;
        this.patrimonio = patrimonio;
        this.qntd_disponivel = qntd_disponivel;
    }

    public int getId_recurso() {
        return id_recurso;
    }

    public void setId_recurso(int id_recurso) {
        this.id_recurso = id_recurso;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipo_recurso() {
        return tipo_recurso;
    }

    public void setTipo_recurso(String tipo_recurso) {
        this.tipo_recurso = tipo_recurso;
    }

    public String getPatrimonio() {
        return patrimonio;
    }

    public void setPatrimonio(String patrimonio) {
        this.patrimonio = patrimonio;
    }

    public int getQntd_disponivel() {
        return qntd_disponivel;
    }

    public void setQntd_disponivel(int qntd_disponivel) {
        this.qntd_disponivel = qntd_disponivel;
    }

}
