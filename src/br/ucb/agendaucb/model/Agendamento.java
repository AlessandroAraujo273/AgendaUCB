package br.ucb.agendaucb.model;

import java.sql.Timestamp;
import br.ucb.agendaucb.model.enums.SituacaoAgendamento;

public class Agendamento {
    private int idAgendamento;
    private Timestamp inicio;
    private Timestamp fim;
    private SituacaoAgendamento situacao;
    private String motivo;
    private Timestamp dataCriacao;
    private int idUsuario;
    private int idPredio;
    private String numeroSala;
    private int idTipo;

    public Agendamento() {
    }

    public Agendamento(int idAgendamento, Timestamp inicio, Timestamp fim, SituacaoAgendamento situacao,
            String motivo, Timestamp dataCriacao, int idUsuario, int idPredio,
            String numeroSala, int idTipo) {
        this.idAgendamento = idAgendamento;
        this.inicio = inicio;
        this.fim = fim;
        this.situacao = situacao;
        this.motivo = motivo;
        this.dataCriacao = dataCriacao;
        this.idUsuario = idUsuario;
        this.idPredio = idPredio;
        this.numeroSala = numeroSala;
        this.idTipo = idTipo;
    }

    public int getIdAgendamento() {
        return idAgendamento;
    }

    public void setIdAgendamento(int idAgendamento) {
        this.idAgendamento = idAgendamento;
    }

    public Timestamp getInicio() {
        return inicio;
    }

    public void setInicio(Timestamp inicio) {
        this.inicio = inicio;
    }

    public Timestamp getFim() {
        return fim;
    }

    public void setFim(Timestamp fim) {
        this.fim = fim;
    }

    public SituacaoAgendamento getSituacao() {
        return situacao;
    }

    public void setSituacao(SituacaoAgendamento situacao) {
        this.situacao = situacao;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public Timestamp getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(Timestamp dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getIdPredio() {
        return idPredio;
    }

    public void setIdPredio(int idPredio) {
        this.idPredio = idPredio;
    }

    public String getNumeroSala() {
        return numeroSala;
    }

    public void setNumeroSala(String numeroSala) {
        this.numeroSala = numeroSala;
    }

    public int getIdTipo() {
        return idTipo;
    }

    public void setIdTipo(int idTipo) {
        this.idTipo = idTipo;
    }
}