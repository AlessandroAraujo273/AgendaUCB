package br.ucb.agendaucb.model;

import br.ucb.agendaucb.model.enums.TipoSala;

public class Sala {
    private int id_predio;
    private String numero_sala;
    private int capacidade;
    private TipoSala tipo_sala;
    private boolean possui_projetor;

    public Sala() {
    };

    public Sala(int id_predio, String numero_sala, int capacidade, TipoSala tipo_sala, boolean possui_projetor) {
        this.id_predio = id_predio;
        this.numero_sala = numero_sala;
        this.capacidade = capacidade;
        this.tipo_sala = tipo_sala;
        this.possui_projetor = possui_projetor;
    }

    public int getId_predio() {
        return id_predio;
    }

    public void setId_predio(int id_predio) {
        this.id_predio = id_predio;
    }

    public String getNumero_sala() {
        return numero_sala;
    }

    public void setNumero_sala(String numero_sala) {
        this.numero_sala = numero_sala;
    }

    public int getCapacidade() {
        return capacidade;
    }

    public void setCapacidade(int capacidade) {
        this.capacidade = capacidade;
    }

    public TipoSala getTipo_sala() {
        return tipo_sala;
    }

    public void setTipo_sala(TipoSala tipo_sala) {
        this.tipo_sala = tipo_sala;
    }

    public boolean isPossui_projetor() {
        return possui_projetor;
    }

    public void setPossui_projetor(boolean possui_projetor) {
        this.possui_projetor = possui_projetor;
    }

}
