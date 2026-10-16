package br.ucb.agendaucb.model;

public class Predio {
    private int id_predio;
    private String nome;
    private String logradouro;
    private String cep;
    private int qntd_andares;

    public Predio() {
    };

    public Predio(int id_predio, String nome, String logradouro, String cep, int qntd_andares) {
        this.id_predio = id_predio;
        this.nome = nome;
        this.logradouro = logradouro;
        this.cep = cep;
        this.qntd_andares = qntd_andares;
    }

    public int getId_predio() {
        return id_predio;
    }

    public void setId_predio(int id_predio) {
        this.id_predio = id_predio;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public int getQntd_andares() {
        return qntd_andares;
    }

    public void setQntd_andares(int qntd_andares) {
        this.qntd_andares = qntd_andares;
    }

}
