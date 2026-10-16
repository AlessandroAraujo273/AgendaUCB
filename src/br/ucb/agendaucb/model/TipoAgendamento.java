package br.ucb.agendaucb.model;

public class TipoAgendamento {
    private int id_tipo;
    private String descricao;
    private int duracao_minutos;
    private boolean aprovacao;

    public TipoAgendamento() {
    };

    public TipoAgendamento(int id_tipo, String descricao, int duracao_minutos, boolean aprovacao) {
        this.id_tipo = id_tipo;
        this.descricao = descricao;
        this.duracao_minutos = duracao_minutos;
        this.aprovacao = aprovacao;
    }

    public int getId_tipo() {
        return id_tipo;
    }

    public void setId_tipo(int id_tipo) {
        this.id_tipo = id_tipo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public int getDuracao_minutos() {
        return duracao_minutos;
    }

    public void setDuracao_minutos(int duracao_minutos) {
        this.duracao_minutos = duracao_minutos;
    }

    public boolean isAprovacao() {
        return aprovacao;
    }

    public void setAprovacao(boolean aprovacao) {
        this.aprovacao = aprovacao;
    }

}
