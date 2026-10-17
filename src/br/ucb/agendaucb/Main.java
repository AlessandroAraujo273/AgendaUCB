package br.ucb.agendaucb;

import br.ucb.agendaucb.dao.PredioDAO;
import br.ucb.agendaucb.dao.RecursoDAO;
import br.ucb.agendaucb.dao.SalaDAO;
import br.ucb.agendaucb.dao.TipoAgendamentoDAO;
import br.ucb.agendaucb.model.Predio;
import br.ucb.agendaucb.model.Recurso;
import br.ucb.agendaucb.model.Sala;
import br.ucb.agendaucb.model.TipoAgendamento;
import br.ucb.agendaucb.model.enums.TipoRecurso;
import br.ucb.agendaucb.model.enums.TipoSala;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        PredioDAO predioDAO = new PredioDAO();
        SalaDAO salaDAO = new SalaDAO();
        RecursoDAO recursoDAO = new RecursoDAO();
        TipoAgendamentoDAO tipoAgendamentoDAO = new TipoAgendamentoDAO();

        try {
            // ==========================================
            // 1. TESTES DO PREDIO DAO
            // ==========================================
            System.out.println("=== 1. TESTANDO PREDIO DAO ===");
            // CEP alterado para "72000000" (8 dígitos) sem o hífen para respeitar a coluna CHAR(8)/VARCHAR(8)
            Predio p1 = new Predio(0, "Bloco X - Testes", "Campus Taguatinga", "72000000", 3);
            predioDAO.cadastrar(p1);
            System.out.println("✅ Prédio cadastrado!");

            List<Predio> predios = predioDAO.listarTudo();
            int idPredioTeste = predios.get(predios.size() - 1).getId_predio();
            System.out.println("-> ID do Prédio de teste obtido: " + idPredioTeste);

            // ==========================================
            // 2. TESTES DO SALA DAO (Chave Composta + Enum)
            // ==========================================
            System.out.println("\n=== 2. TESTANDO SALA DAO ===");
            Sala salaTemp = new Sala(idPredioTeste, "SALA-TESTE", 30, TipoSala.LABORATORIO, true);
            salaDAO.cadastrar(salaTemp);
            System.out.println("✅ Sala 'SALA-TESTE' cadastrada no Prédio " + idPredioTeste);

            Sala salaEncontrada = salaDAO.buscarPorChave(idPredioTeste, "SALA-TESTE");
            if (salaEncontrada != null) {
                System.out.println("-> Sala encontrada via chave composta: " + salaEncontrada.getNumero_sala() + " | Tipo: " + salaEncontrada.getTipo_sala());
                
                salaEncontrada.setTipo_sala(TipoSala.AUDITORIO);
                salaEncontrada.setCapacidade(60);
                salaDAO.atualizar(salaEncontrada);
                System.out.println("✅ Sala atualizada para AUDITORIO via chave composta!");
            }

            // ==========================================
            // 3. TESTES DO RECURSO DAO (Enum TipoRecurso)
            // ==========================================
            System.out.println("\n=== 3. TESTANDO RECURSO DAO ===");
            Recurso recursoTemp = new Recurso(0, "Kit Cabo HDMI 10m", TipoRecurso.CONSUMO, "PAT-TESTE", 10);
            recursoDAO.cadastrar(recursoTemp);
            System.out.println("✅ Recurso cadastrado!");

            List<Recurso> recursos = recursoDAO.listarTudo();
            int idRecursoTeste = recursos.get(recursos.size() - 1).getId_recurso();
            
            Recurso recEncontrado = recursoDAO.buscarPorId(idRecursoTeste);
            if (recEncontrado != null) {
                System.out.println("-> Recurso encontrado: " + recEncontrado.getNome() + " | Tipo: " + recEncontrado.getTipo_recurso());
                
                recEncontrado.setTipo_recurso(TipoRecurso.EQUIPAMENTO);
                recursoDAO.atualizar(recEncontrado);
                System.out.println("✅ Recurso atualizado para EQUIPAMENTO!");
            }

            // ==========================================
            // 4. TESTES DO TIPO AGENDAMENTO DAO
            // ==========================================
            System.out.println("\n=== 4. TESTANDO TIPO AGENDAMENTO DAO ===");
            TipoAgendamento tipoTemp = new TipoAgendamento(0, "Reunião de Alinhamento", 60, false);
            tipoAgendamentoDAO.cadastrar(tipoTemp);
            System.out.println("✅ Tipo de agendamento cadastrado!");

            List<TipoAgendamento> tipos = tipoAgendamentoDAO.listarTudo();
            int idTipoTeste = tipos.get(tipos.size() - 1).getId_tipo();

            TipoAgendamento tipoEncontrado = tipoAgendamentoDAO.buscarPorId(idTipoTeste);
            if (tipoEncontrado != null) {
                System.out.println("-> Tipo Agendamento encontrado: " + tipoEncontrado.getDescricao());
                
                tipoEncontrado.setDuracao_minutos(90);
                tipoAgendamentoDAO.atualizar(tipoEncontrado);
                System.out.println("✅ Duração atualizada para 90 minutos!");
            }

            // ==========================================
            // 5. LIMPEZA DOS DADOS DE TESTE (DELETES)
            // ==========================================
            System.out.println("\n=== 5. EXECUTANDO LIMPEZA DOS REGISTROS DE TESTE ===");
            
            salaDAO.deletar(idPredioTeste, "SALA-TESTE");
            System.out.println("✅ Sala de teste removida via chave composta.");

            predioDAO.deletar(idPredioTeste);
            System.out.println("✅ Prédio de teste removido.");

            recursoDAO.deletar(idRecursoTeste);
            System.out.println("✅ Recurso de teste removido.");

            tipoAgendamentoDAO.deletar(idTipoTeste);
            System.out.println("✅ Tipo de agendamento de teste removido.");

            System.out.println("\n🎉 TODOS OS TESTES DOS 4 DAOs FORAM CONCLUÍDOS COM SUCESSO!");

        } catch (Exception e) {
            System.err.println("❌ Erro durante os testes integrados:");
            e.printStackTrace();
        }
    }
}