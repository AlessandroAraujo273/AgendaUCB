package br.ucb.agendaucb;

import br.ucb.agendaucb.dao.AgendamentoDAO;
import br.ucb.agendaucb.dao.PredioDAO;
import br.ucb.agendaucb.dao.SalaDAO;
import br.ucb.agendaucb.dao.TipoAgendamentoDAO;
import br.ucb.agendaucb.dao.UsuarioDAO;
import br.ucb.agendaucb.model.Agendamento;
import br.ucb.agendaucb.model.Predio;
import br.ucb.agendaucb.model.Sala;
import br.ucb.agendaucb.model.TipoAgendamento;
import br.ucb.agendaucb.model.Usuario;
import br.ucb.agendaucb.model.enums.SituacaoAgendamento;
import br.ucb.agendaucb.model.enums.TipoSala;

import java.sql.Timestamp;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        PredioDAO predioDAO = new PredioDAO();
        SalaDAO salaDAO = new SalaDAO();
        TipoAgendamentoDAO tipoDAO = new TipoAgendamentoDAO();
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        AgendamentoDAO agendamentoDAO = new AgendamentoDAO();

        try {
            // ==========================================
            // 1. GARANTIR A ESTRUTURA BASE (PREDIO, SALA, TIPO)
            // ==========================================
            System.out.println("=== 1. VERIFICANDO ESTRUTURA BASE (PREDIO / SALA / TIPO) ===");

            // Prédio
            List<Predio> predios = predioDAO.listarTudo();
            int idPredio;
            if (predios.isEmpty()) {
                Predio predio = new Predio(0, "Bloco Central", "Campus Taguatinga", "72000000", 3);
                predioDAO.cadastrar(predio);
                predios = predioDAO.listarTudo();
            }
            idPredio = predios.get(0).getId_predio();

            // Sala
            Sala salaFixa = salaDAO.buscarPorChave(idPredio, "LAB-101");
            if (salaFixa == null) {
                salaFixa = new Sala(idPredio, "LAB-101", 40, TipoSala.LABORATORIO, true);
                salaDAO.cadastrar(salaFixa);
                System.out.println("[OK] Sala 'LAB-101' criada para o Predio ID " + idPredio);
            }

            // Tipo de Agendamento
            List<TipoAgendamento> tipos = tipoDAO.listarTudo();
            int idTipo;
            if (tipos.isEmpty()) {
                TipoAgendamento tipo = new TipoAgendamento(0, "Reuniao de Projeto", 60, false);
                tipoDAO.cadastrar(tipo);
                tipos = tipoDAO.listarTudo();
            }
            idTipo = tipos.get(0).getId_tipo();

            System.out.println("[OK] Estrutura base pronta: Predio ID " + idPredio + " | Sala LAB-101 | Tipo ID " + idTipo);

            // ==========================================
            // 2. AUTENTICAR O USUÁRIO DO SCRIPT SQL
            // ==========================================
            System.out.println("\n=== 2. TENTANDO AUTENTICAR O USUÁRIO DO BANCO ===");

            String emailInstitucional = "atila.batista@ucb.br";
            String senhaHash = "hash_senha_123";

            Usuario usuarioLogado = usuarioDAO.autenticar(emailInstitucional, senhaHash);

            if (usuarioLogado != null) {
                System.out.println("[OK] Autenticacao realizada com sucesso!");
                System.out.println(" -> ID Recuperado do Banco: " + usuarioLogado.getId_usuario());
                System.out.println(" -> Nome: " + usuarioLogado.getNome() + " " + usuarioLogado.getSobrenome());
                System.out.println(" -> Email: " + usuarioLogado.getEmail_institucional());
                System.out.println(" -> Perfil: " + usuarioLogado.getTipo_usuario());
            } else {
                System.err.println("[ERRO] Usuario nao encontrado! Certifique-se de ter executado o script INSERT no MySQL.");
                return;
            }

            // ==========================================
            // 3. AGENDAR COM O USUÁRIO AUTENTICADO
            // ==========================================
            System.out.println("\n=== 3. CRIANDO AGENDAMENTO COM O USUÁRIO AUTENTICADO ===");

            // Define horários dinâmicos sem risco de conflitos
            long tempoAtual = System.currentTimeMillis();
            Timestamp inicio = new Timestamp(tempoAtual + 86400000L); // Amanhã neste horário
            Timestamp fim = new Timestamp(tempoAtual + 90000000L);    // +1h horas depois
            Timestamp agora = new Timestamp(tempoAtual);

            Agendamento novoAgendamento = new Agendamento(
                    0,
                    inicio,
                    fim,
                    SituacaoAgendamento.SOLICITADO,
                    "Reserva para testes do sistema via Java",
                    agora,
                    usuarioLogado.getId_usuario(),
                    idPredio,
                    "LAB-101",
                    idTipo
            );

            boolean cadastrou = agendamentoDAO.cadastrar(novoAgendamento);

            if (cadastrou) {
                System.out.println("[OK] Agendamento gravado no banco de dados com sucesso!");
            } else {
                System.out.println("[AVISO] Nao foi possivel agendar devido a conflito de horario.");
            }

            // ==========================================
            // 4. CONSULTA FINAL DOS REGISTROS
            // ==========================================
            System.out.println("\n=== 4. LISTA DE AGENDAMENTOS NO BANCO ===");

            List<Agendamento> listaAgendamentos = agendamentoDAO.listarTudo();
            for (Agendamento a : listaAgendamentos) {
                System.out.println("ID: " + a.getIdAgendamento() +
                                   " | Solicitante (Id_usuario): " + a.getIdUsuario() +
                                   " | Sala: " + a.getNumeroSala() +
                                   " | Inicio: " + a.getInicio() +
                                   " | Status: " + a.getSituacao());
            }

            System.out.println("\n[SUCESSO] Fluxo de login e agendamento concluido!");

        } catch (Exception e) {
            System.err.println("[ERRO] Falha durante a execucao do teste:");
            e.printStackTrace();
        }
    }
}