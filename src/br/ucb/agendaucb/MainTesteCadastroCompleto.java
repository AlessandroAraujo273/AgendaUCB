package br.ucb.agendaucb;

import java.sql.Date;

import br.ucb.agendaucb.dao.UsuarioDAO;
import br.ucb.agendaucb.model.Administrativo;
import br.ucb.agendaucb.model.Aluno;
import br.ucb.agendaucb.model.Professor;
import br.ucb.agendaucb.model.enums.SituacaoAcademica;
import br.ucb.agendaucb.model.enums.TipoUsuario;

public class MainTesteCadastroCompleto {

    public static void main(String[] args) {
        try {
            UsuarioDAO dao = new UsuarioDAO();

            // 1. Teste de Cadastro de Aluno
            Aluno aluno = new Aluno();
            aluno.setCpf("12345678909");
            aluno.setNome("Mariana");
            aluno.setSobrenome("Souza");
            aluno.setEmail_institucional("mariana.pinto@ucb.br");
            aluno.setSenha_hash("$2a$10$e832y98d3y281397y12398y1");
            aluno.setData_nascimento(Date.valueOf("2003-04-12"));
            aluno.setTipo_usuario(TipoUsuario.ALUNO);
            aluno.setId_departamento(1);
            aluno.setMatricula("UCB20260007");
            aluno.setCurso("Análise e Desenvolvimento de Sistemas");
            aluno.setSemestre_atual(2);
            aluno.setSituacaoAcademica(SituacaoAcademica.ATIVO);

            dao.cadastrar(aluno, "61991112222", "CELULAR");
            System.out.println("[ALUNO] Registrado: " + aluno.getNome() + " (" + aluno.getEmail_institucional() + ") - ID: " + aluno.getId_usuario());

            // 2. Teste de Cadastro de Professor
            Professor professor = new Professor();
            professor.setCpf("98765432100");
            professor.setNome("Roberto");
            professor.setSobrenome("Alves");
            professor.setEmail_institucional("roberto.alves@ucb.br");
            professor.setSenha_hash("$2a$10$e832y98d3y281397y12398y1");
            professor.setData_nascimento(Date.valueOf("1980-11-25"));
            professor.setTipo_usuario(TipoUsuario.PROFESSOR);
            professor.setId_departamento(1);
            professor.setMatricula_docente("DOC2026002");
            professor.setTitulacao("Mestre");
            professor.setRegimeTrabalho("20 horas");

            dao.cadastrar(professor, "6133334444", "FIXO");
            System.out.println("[PROFESSOR] Registrado: " + professor.getNome() + " (" + professor.getEmail_institucional() + ") - ID: " + professor.getId_usuario());

            // 3. Teste de Cadastro de Administrativo
            Administrativo adm = new Administrativo();
            adm.setCpf("45678912300");
            adm.setNome("Fernanda");
            adm.setSobrenome("Lima");
            adm.setEmail_institucional("fernanda.lima@ucb.br");
            adm.setSenha_hash("$2a$10$e832y98d3y281397y12398y1");
            adm.setData_nascimento(Date.valueOf("1992-07-08"));
            adm.setTipo_usuario(TipoUsuario.ADMINISTRATIVO);
            adm.setId_departamento(1);
            adm.setCargo("Coordenadora Acadêmica");
            adm.setSetor("Secretaria Geral");
            adm.setRamal("4012");

            dao.cadastrar(adm, "4012", "RAMAL");
            System.out.println("[ADMINISTRATIVO] Registrado: " + adm.getNome() + " (" + adm.getEmail_institucional() + ") - ID: " + adm.getId_usuario());

            System.out.println("\n--- Todos os 3 perfis foram persistidos com sucesso via Transação JDBC! ---");

        } catch (Exception e) {
            System.err.println("Erro ao executar o teste de cadastro: " + e.getMessage());
            e.printStackTrace();
        }
    }
}