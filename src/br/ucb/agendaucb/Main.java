package br.ucb.agendaucb;

import br.ucb.agendaucb.dao.PredioDAO;
import br.ucb.agendaucb.model.Predio;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        PredioDAO predioDAO = new PredioDAO();

        try {
            System.out.println("=== 1. CADASTRANDO PRÉDIO FIXO (Para você conferir no MySQL) ===");
            Predio predioFixo = new Predio(0, "Bloco A - Principal", "QS 07 Lote 01", "71966700", 5);
            predioDAO.cadastrar(predioFixo);
            System.out.println("✅ Prédio Fixo cadastrado! Este PERMANECE no banco.");

            System.out.println("\n=== 2. CADASTRANDO PRÉDIO TEMPORÁRIO (Para testar exclusão) ===");
            Predio predioTemp = new Predio(0, "Bloco B - Teste Exclusão", "QS 07 Lote 01", "71966700", 2);
            predioDAO.cadastrar(predioTemp);
            System.out.println("✅ Prédio Temporário cadastrado com sucesso!");

            System.out.println("\n=== 3. LISTANDO TODOS OS PRÉDIOS NO BANCO ===");
            List<Predio> predios = predioDAO.listarTudo();
            for (Predio p : predios) {
                System.out.println("ID: " + p.getId_predio() + " | Nome: " + p.getNome() + " | Andares: " + p.getQntd_andares());
            }

            if (!predios.isEmpty()) {
                // Pega o último registro cadastrado (predioTemp) para realizar os testes de edição e deleção
                int idParaDeletar = predios.get(predios.size() - 1).getId_predio();

                System.out.println("\n=== 4. TESTANDO ATUALIZAÇÃO NO PRÉDIO TEMPORÁRIO (ID: " + idParaDeletar + ") ===");
                Predio encontrado = predioDAO.buscarPorId(idParaDeletar);
                if (encontrado != null) {
                    encontrado.setNome(encontrado.getNome() + " [Editado]");
                    predioDAO.atualizar(encontrado);
                    System.out.println("✅ Prédio temporário atualizado com sucesso!");

                    System.out.println("\n=== 5. DELETANDO APENAS O PRÉDIO TEMPORÁRIO (ID: " + idParaDeletar + ") ===");
                    predioDAO.deletar(idParaDeletar);
                    System.out.println("✅ Prédio temporário removido do banco!");
                }
            }

            System.out.println("\n=== 6. LISTAGEM FINAL DE CONFERÊNCIA ===");
            System.out.println("Registros ativos no banco após os testes:");
            for (Predio p : predioDAO.listarTudo()) {
                System.out.println("-> ID: " + p.getId_predio() + " | Nome: " + p.getNome());
            }

        } catch (Exception e) {
            System.err.println("❌ Erro durante a execução dos testes:");
            e.printStackTrace();
        }
    }
}