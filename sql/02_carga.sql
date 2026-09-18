-- ==============================================================================
-- AgendaUCB — Script de Carga de Dados (DML)
-- Compatível com 01_ddl.sql. Insere dados coerentes, realistas e casos de contorno.
-- ==============================================================================

USE agendaucb;

-- Desativa temporariamente a verificação de chaves estrangeiras para garantir ordem livre de inserção se necessário,
-- embora a ordem abaixo já respeite totalmente as dependências.
SET FOREIGN_KEY_CHECKS = 0;

-- Limpa dados anteriores caso o script seja reexecutado na mesma base
TRUNCATE TABLE participacao_agendamento;
TRUNCATE TABLE agendamento_recurso;
TRUNCATE TABLE historico_status_agendamento;
TRUNCATE TABLE agendamento;
TRUNCATE TABLE sala;
TRUNCATE TABLE telefone_usuario;
TRUNCATE TABLE usuario;
TRUNCATE TABLE recurso;
TRUNCATE TABLE tipo_agendamento;
TRUNCATE TABLE predio;
TRUNCATE TABLE departamento;

SET FOREIGN_KEY_CHECKS = 1;

-- ==============================================================================
-- 1. DEPARTAMENTO (RN06, RN07)
-- Inclui departamento pai e filhos para demonstrar o autorrelacionamento hierárquico.
-- ==============================================================================
INSERT INTO departamento (id_departamento, nome, sigla, data_criacao, id_departamento_pai) VALUES
(1, 'Decanato de Engenharia e Ciências Exatas', 'DECE', '2015-02-01', NULL),
(2, 'Faculdade de Engenharia de Software', 'FES', '2018-03-10', 1),
(3, 'Faculdade de Ciência da Computação', 'FCC', '2018-03-10', 1),
(4, 'Núcleo de Tecnologia e Suporte Computacional', 'NTSC', '2016-07-15', NULL),
(5, 'Secretaria Acadêmica Central', 'SEC', '2012-01-10', NULL);

-- ==============================================================================
-- 2. PRÉDIO (RN10)
-- Mínimo de 1 andar cadastrado por prédio.
-- ==============================================================================
INSERT INTO predio (id_predio, nome, logradouro, cep, quantidade_andares) VALUES
(1, 'Bloco Central de Laboratórios', 'QS 07 Lote 01 Taguatinga Sul', '72030170', 3),
(2, 'Torre Administrativa e de Salas de Aula', 'QS 07 Lote 01 Taguatinga Sul', '72030170', 5),
(3, 'Centro de Vivência e Multiuso', 'QS 07 Lote 01 Taguatinga Sul', '72030170', 2);

-- ==============================================================================
-- 3. TIPO_AGENDAMENTO (RN13)
-- Define quais tipos exigem aprovação prévia.
-- ==============================================================================
INSERT INTO tipo_agendamento (id_tipo, descricao, duracao_padrao_minutos, requer_aprovacao) VALUES
(1, 'Aula Prática Regular', 100, 0),
(2, 'Reserva de Laboratório Especial para Pesquisa', 120, 1),
(3, 'Reunião de Colegiado de Curso', 60, 1),
(4, 'Defesa de Trabalho de Conclusão de Curso (TCC)', 90, 1),
(5, 'Estudo em Grupo / Monitoria', 60, 0);

-- ==============================================================================
-- 4. RECURSO (RN14)
-- Equipamentos, itens de consumo e chaves.
-- ==============================================================================
INSERT INTO recurso (id_recurso, nome, tipo_recurso, patrimonio, quantidade_total_disponivel) VALUES
(1, 'Projetor Multimídia Portátil Epson', 'EQUIPAMENTO', 'PAT-98210', 5),
(2, 'Chave Mestra do Bloco de Laboratórios', 'CHAVE', 'CHV-0102', 2),
(3, 'Kit de Conectores de Rede RJ45 e Crimpador', 'CONSUMO', NULL, 15),
(4, 'Microfone Sem Fio Duplo AKG', 'EQUIPAMENTO', 'PAT-55412', 4),
(5, 'Notebook Dell Latitude i7 (Uso Didático)', 'EQUIPAMENTO', 'PAT-33201', 10);

-- ==============================================================================
-- 5. USUÁRIO (RN01, RN02, RN03, RN04, RN05, RN08, RN19)
-- Especialização em tabela única: ALUNO, PROFESSOR, ADMINISTRATIVO.
-- Casos de contorno: atributos opcionais nulos (ex: ramal ou curso em branco para perfis não aplicáveis).
-- ==============================================================================
INSERT INTO usuario (id_usuario, cpf, nome, sobrenome, email_institucional, senha_hash, data_nascimento, tipo_usuario, id_departamento, matricula, curso, semestre_atual, situacao_academica, matricula_docente, titulacao, regime_trabalho, cargo, setor, ramal) VALUES
-- Alunos (FES e FCC)
(1, '11144477735', 'Lucas', 'Oliveira Santos', 'lucas.santos@estudante.ucb.br', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '2003-05-14', 'ALUNO', 2, 'UCB2024101', 'Engenharia de Software', 5, 'ATIVO', NULL, NULL, NULL, NULL, NULL, NULL),
(2, '22255588846', 'Mariana', 'Costa e Silva', 'mariana.silva@estudante.ucb.br', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '2004-11-20', 'ALUNO', 2, 'UCB2023255', 'Engenharia de Software', 7, 'ATIVO', NULL, NULL, NULL, NULL, NULL, NULL),
(3, '33366699957', 'Gabriel', 'Almeida Souza', 'gabriel.souza@estudante.ucb.br', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '2002-02-10', 'ALUNO', 3, 'UCB2022198', 'Ciência da Computação', 9, 'TRANCADO', NULL, NULL, NULL, NULL, NULL, NULL),
(4, '44477700068', 'Beatriz', 'Lima Ribeiro', 'beatriz.ribeiro@estudante.ucb.br', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '2005-08-30', 'ALUNO', 2, 'UCB2025012', 'Engenharia de Software', 2, 'ATIVO', NULL, NULL, NULL, NULL, NULL, NULL),

-- Professores
(5, '55588811179', 'Samuel', 'Novais Moura Júnior', 'samuel.moura@p.ucb.br', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '1982-04-15', 'PROFESSOR', 2, NULL, NULL, NULL, NULL, 'DOC-8821', 'Doutor', 'Dedicação Exclusiva', NULL, NULL, NULL),
(6, '66699922280', 'Carla', 'Menezes Pires', 'carla.pires@p.ucb.br', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '1979-09-12', 'PROFESSOR', 3, NULL, NULL, NULL, NULL, 'DOC-9102', 'Mestre', '40 horas', NULL, NULL, NULL),
(7, '77700033391', 'Roberto', 'Carlos Siqueira', 'roberto.siqueira@p.ucb.br', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '1975-12-03', 'PROFESSOR', 2, NULL, NULL, NULL, NULL, 'DOC-7412', 'Doutor', 'Dedicação Exclusiva', NULL, NULL, NULL),

-- Administrativos
(8, '88811144402', 'Juliana', 'Martins Azevedo', 'juliana.azevedo@ucb.br', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '1990-06-25', 'ADMINISTRATIVO', 5, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Assistente de Secretaria', 'Secretaria Geral', '4401'),
(9, '99922255513', 'Marcos', 'Vinícius Rocha', 'marcos.rocha@ucb.br', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '1988-01-18', 'ADMINISTRATIVO', 4, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Técnico de Suporte TI', 'Suporte Técnico', '4488');

-- ==============================================================================
-- 6. TELEFONE_USUARIO (RN09)
-- Múltiplos telefones por utilizador (entidade fraca). Casos com omissão de ramais ou fixos.
-- ==============================================================================
INSERT INTO telefone_usuario (id_usuario, numero_telefone, tipo_telefone) VALUES
(1, '(61) 98877-1122', 'CELULAR'),
(2, '(61) 99233-4455', 'CELULAR'),
(2, '(61) 3356-9900', 'FIXO'),
(3, '(61) 98111-2233', 'CELULAR'),
(5, '(61) 99988-7766', 'CELULAR'),
(5, '(61) 3356-9001', 'RAMAL'),
(8, '(61) 98444-3322', 'CELULAR'),
(8, '(61) 3356-4401', 'RAMAL'),
(9, '(61) 98222-1144', 'CELULAR');

-- ==============================================================================
-- 7. SALA (RN11, RN12)
-- Relacionada exatamente a um prédio, com número único no prédio.
-- ==============================================================================
INSERT INTO sala (numero_sala, id_predio, capacidade, tipo_sala, possui_projetor) VALUES
('LAB-101', 1, 40, 'LABORATORIO', 1),
('LAB-102', 1, 35, 'LABORATORIO', 1),
('AUD-01', 3, 150, 'AUDITORIO', 1),
('SALA-205', 2, 50, 'SALA_DE_AULA', 1),
('SALA-206', 2, 50, 'SALA_DE_AULA', 0),
('SALA-REU1', 2, 15, 'REUNIAO', 1);

-- ==============================================================================
-- 8. AGENDAMENTO (RN15, RN16, RN17)
-- Datas de início estritamente anteriores ao término.
-- ==============================================================================
INSERT INTO agendamento (id_agendamento, data_hora_inicio, data_hora_fim, situacao_atual, motivo_descricao, data_criacao, id_usuario_solicitante, id_predio, numero_sala, id_tipo) VALUES
(1, '2026-10-01 08:00:00', '2026-10-01 09:40:00', 'REALIZADO', 'Aula prática de laboratório de banco de dados', '2026-09-25 10:00:00', 5, 1, 'LAB-101', 1),
(2, '2026-10-02 14:00:00', '2026-10-02 16:00:00', 'APROVADO', 'Pesquisa experimental com banco de dados distribuídos', '2026-09-26 11:30:00', 6, 1, 'LAB-102', 2),
(3, '2026-10-05 10:00:00', '2026-10-05 11:00:00', 'SOLICITADO', 'Reunião de planejamento de sprint do colegiado', '2026-09-28 15:45:00', 7, 2, 'SALA-REU1', 3),
(4, '2026-10-10 09:00:00', '2026-10-10 10:30:00', 'CONFIRMADO', 'Apresentação de TCC - Grupo AgendaUCB', '2026-09-29 09:15:00', 5, 3, 'AUD-01', 4),
(5, '2026-10-12 14:00:00', '2026-10-12 15:00:00', 'CANCELADO', 'Sessão de estudos em grupo cancelada por motivo de força maior', '2026-09-30 08:20:00', 1, 2, 'SALA-205', 5),
(6, '2026-10-15 19:00:00', '2026-10-15 21:00:00', 'SOLICITADO', 'Defesa pública de TCC - Turma Noturna', '2026-10-01 14:00:00', 6, 3, 'AUD-01', 4);

-- ==============================================================================
-- 9. HISTORICO_STATUS_AGENDAMENTO (RN18)
-- Atributo temporal (situação ao longo do tempo). Casos com mais de um evento de histórico.
-- ==============================================================================
INSERT INTO historico_status_agendamento (id_agendamento, numero_sequencia, situacao_anterior, situacao_nova, data_hora_mudanca, observacao, id_usuario_responsavel) VALUES
-- Agendamento 1 (Passou por solicitado, aprovado, confirmado e realizado)
(1, 1, NULL, 'SOLICITADO', '2026-09-25 10:00:00', 'Solicitação inicial aberta pelo docente', 5),
(1, 2, 'SOLICITADO', 'APROVADO', '2026-09-25 14:00:00', 'Aprovado automaticamente pelo sistema de aulas', 8),
(1, 3, 'APROVADO', 'CONFIRMADO', '2026-09-26 08:00:00', 'Confirmação de recursos e chaves entregues', 9),
(1, 4, 'CONFIRMADO', 'REALIZADO', '2026-10-01 09:45:00', 'Atividade concluída com sucesso', 5),

-- Agendamento 2 (Solicitado e Aprovado)
(2, 1, NULL, 'SOLICITADO', '2026-09-26 11:30:00', 'Solicitação de laboratório especial', 6),
(2, 2, 'SOLICITADO', 'APROVADO', '2026-09-27 09:00:00', 'Aprovado pela coordenação do departamento', 8),

-- Agendamento 3 (Apenas solicitado)
(3, 1, NULL, 'SOLICITADO', '2026-09-28 15:45:00', 'Solicitação de reunião enviada', 7),

-- Agendamento 4 (Solicitado, Aprovado e Confirmado)
(4, 1, NULL, 'SOLICITADO', '2026-09-29 09:15:00', 'Solicitação de auditorio para TCC', 5),
(4, 2, 'SOLICITADO', 'APROVADO', '2026-09-29 11:00:00', 'Aprovado pela infraestrutura', 8),
(4, 3, 'APROVADO', 'CONFIRMADO', '2026-09-29 16:00:00', 'Equipamentos de som reservados', 9),

-- Agendamento 5 (Solicitado e depois Cancelado)
(5, 1, NULL, 'SOLICITADO', '2026-09-30 08:20:00', 'Reserva de sala de aula para estudo', 1),
(5, 2, 'SOLICITADO', 'CANCELADO', '2026-09-30 18:00:00', 'Cancelado pelo próprio solicitante', 1),

-- Agendamento 6 (Solicitado recentemente)
(6, 1, NULL, 'SOLICITADO', '2026-10-01 14:00:00', 'Solicitação inicial de defesa de TCC', 6);

-- ==============================================================================
-- 10. AGENDAMENTO_RECURSO (RN20)
-- Associação N:N entre agendamento e recurso com atributos próprios (quantidade e devolução).
-- ==============================================================================
INSERT INTO agendamento_recurso (id_agendamento, id_recurso, quantidade_reservada, situacao_devolucao) VALUES
(1, 1, 1, 'DEVOLVIDO'),
(1, 2, 1, 'DEVOLVIDO'),
(2, 1, 1, 'PENDENTE'),
(2, 5, 2, 'PENDENTE'),
(4, 4, 2, 'DEVOLVIDO'),
(4, 1, 1, 'DEVOLVIDO'),
(6, 4, 1, NULL);

-- ==============================================================================
-- 11. PARTICIPACAO_AGENDAMENTO (RN21)
-- Associação N:N entre agendamento e usuário com papéis e status de confirmação.
-- ==============================================================================
INSERT INTO participacao_agendamento (id_agendamento, id_usuario, papel_participante, status_confirmacao, data_confirmacao) VALUES
(1, 1, 'RESPONSAVEL_TECNICO', 'CONFIRMADO', '2026-09-25 10:30:00'),
(1, 2, 'OBSERVADOR', 'CONFIRMADO', '2026-09-25 11:00:00'),
(2, 6, 'RESPONSAVEL_TECNICO', 'CONFIRMADO', '2026-09-26 12:00:00'),
(2, 3, 'CONVIDADO', 'PENDENTE', NULL),
(3, 7, 'RESPONSAVEL_TECNICO', 'CONFIRMADO', '2026-09-28 16:00:00'),
(3, 8, 'CONVIDADO', 'CONFIRMADO', '2026-09-28 17:00:00'),
(4, 5, 'RESPONSAVEL_TECNICO', 'CONFIRMADO', '2026-09-29 09:30:00'),
(4, 1, 'CONVIDADO', 'CONFIRMADO', '2026-09-29 10:00:00'),
(4, 2, 'CONVIDADO', 'CONFIRMADO', '2026-09-29 10:05:00'),
(6, 6, 'RESPONSAVEL_TECNICO', 'CONFIRMADO', '2026-10-01 14:30:00'),
(6, 4, 'CONVIDADO', 'PENDENTE', NULL);