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
TRUNCATE TABLE aluno;
TRUNCATE TABLE professor;
TRUNCATE TABLE administrativo;
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
-- Especialização em múltiplas tabelas: ALUNO, PROFESSOR, ADMINISTRATIVO.
-- Casos de contorno: atributos opcionais nulos (ex: ramal ou curso em branco para perfis não aplicáveis).
-- ==============================================================================
INSERT INTO usuario (id_usuario, cpf, nome, sobrenome, email_institucional, senha_hash, data_nascimento, tipo_usuario, id_departamento) VALUES
(1, '11144477735', 'Lucas', 'Oliveira Santos', 'lucas.santos@estudante.ucb.br', '$2y$10$hash', '2003-05-14', 'ALUNO', 2),
(2, '22255588846', 'Mariana', 'Costa e Silva', 'mariana.silva@estudante.ucb.br', '$2y$10$hash', '2004-11-20', 'ALUNO', 2),
(3, '33366699957', 'Gabriel', 'Almeida Souza', 'gabriel.souza@estudante.ucb.br', '$2y$10$hash', '2002-02-10', 'ALUNO', 3),
(4, '44477700068', 'Beatriz', 'Lima Ribeiro', 'beatriz.ribeiro@estudante.ucb.br', '$2y$10$hash', '2005-08-30', 'ALUNO', 2),
(5, '10020030040', 'Pedro', 'Henrique Alves', 'pedro.alves@estudante.ucb.br', '$2y$10$hash', '2001-07-22', 'ALUNO', 2),
(6, '10120130141', 'Ana', 'Clara Gomes', 'ana.gomes@estudante.ucb.br', '$2y$10$hash', '2003-03-15', 'ALUNO', 3),
(7, '10220230242', 'João', 'Vitor Martins', 'joao.martins@estudante.ucb.br', '$2y$10$hash', '2002-12-05', 'ALUNO', 2),
(8, '10320330343', 'Luiza', 'Ferreira', 'luiza.ferreira@estudante.ucb.br', '$2y$10$hash', '2004-01-20', 'ALUNO', 2),
(9, '10420430444', 'Mateus', 'Rocha', 'mateus.rocha@estudante.ucb.br', '$2y$10$hash', '2001-09-18', 'ALUNO', 3),
(10, '10520530545', 'Julia', 'Melo', 'julia.melo@estudante.ucb.br', '$2y$10$hash', '2005-05-11', 'ALUNO', 2),
(11, '10620630646', 'Caio', 'Nunes', 'caio.nunes@estudante.ucb.br', '$2y$10$hash', '2000-08-08', 'ALUNO', 3),
(12, '10720730747', 'Leticia', 'Dias', 'leticia.dias@estudante.ucb.br', '$2y$10$hash', '2003-06-21', 'ALUNO', 2),
(13, '10820830848', 'Enzo', 'Castro', 'enzo.castro@estudante.ucb.br', '$2y$10$hash', '2004-10-30', 'ALUNO', 2),
(14, '10920930949', 'Sophia', 'Correia', 'sophia.correia@estudante.ucb.br', '$2y$10$hash', '2002-04-14', 'ALUNO', 3),
(15, '11021031050', 'Thiago', 'Monteiro', 'thiago.monteiro@estudante.ucb.br', '$2y$10$hash', '2001-11-25', 'ALUNO', 2),
(16, '11121131151', 'Isabela', 'Mendes', 'isabela.mendes@estudante.ucb.br', '$2y$10$hash', '2003-02-28', 'ALUNO', 2),
(17, '11221231252', 'Rafael', 'Cardoso', 'rafael.cardoso@estudante.ucb.br', '$2y$10$hash', '2002-07-19', 'ALUNO', 3),
(18, '11321331353', 'Manuela', 'Teixeira', 'manuela.teixeira@estudante.ucb.br', '$2y$10$hash', '2004-12-12', 'ALUNO', 2),
(19, '11421431454', 'Guilherme', 'Cavalcanti', 'guilherme.cavalcanti@estudante.ucb.br', '$2y$10$hash', '2001-03-03', 'ALUNO', 3),
(20, '11521531555', 'Vitoria', 'Pinto', 'vitoria.pinto@estudante.ucb.br', '$2y$10$hash', '2005-09-09', 'ALUNO', 2),
(21, '11621631656', 'Arthur', 'Farias', 'arthur.farias@estudante.ucb.br', '$2y$10$hash', '2000-01-31', 'ALUNO', 2),
(22, '11721731757', 'Giovanna', 'Moura', 'giovanna.moura@estudante.ucb.br', '$2y$10$hash', '2003-10-04', 'ALUNO', 3),
(23, '11821831858', 'Gustavo', 'Borges', 'gustavo.borges@estudante.ucb.br', '$2y$10$hash', '2002-05-17', 'ALUNO', 2),
(24, '11921931959', 'Camila', 'Viana', 'camila.viana@estudante.ucb.br', '$2y$10$hash', '2004-08-22', 'ALUNO', 2),
(25, '12022032060', 'Felipe', 'Andrade', 'felipe.andrade@estudante.ucb.br', '$2y$10$hash', '2001-06-06', 'ALUNO', 3),
(26, '55588811179', 'Samuel', 'Novais Moura Júnior', 'samuel.moura@p.ucb.br', '$2y$10$hash', '1982-04-15', 'PROFESSOR', 2),
(27, '66699922280', 'Carla', 'Menezes Pires', 'carla.pires@p.ucb.br', '$2y$10$hash', '1979-09-12', 'PROFESSOR', 3),
(28, '77700033391', 'Roberto', 'Carlos Siqueira', 'roberto.siqueira@p.ucb.br', '$2y$10$hash', '1975-12-03', 'PROFESSOR', 2),
(29, '20130140150', 'Fernanda', 'Lima', 'fernanda.lima@p.ucb.br', '$2y$10$hash', '1985-02-14', 'PROFESSOR', 2),
(30, '20230240251', 'Ricardo', 'Gomes', 'ricardo.gomes@p.ucb.br', '$2y$10$hash', '1980-11-20', 'PROFESSOR', 3),
(31, '20330340352', 'Patricia', 'Souza', 'patricia.souza@p.ucb.br', '$2y$10$hash', '1978-05-05', 'PROFESSOR', 2),
(32, '20430440453', 'Marcelo', 'Ribeiro', 'marcelo.ribeiro@p.ucb.br', '$2y$10$hash', '1983-08-30', 'PROFESSOR', 3),
(33, '20530540554', 'Daniela', 'Carvalho', 'daniela.carvalho@p.ucb.br', '$2y$10$hash', '1988-01-10', 'PROFESSOR', 2),
(34, '20630640655', 'Eduardo', 'Martins', 'eduardo.martins@p.ucb.br', '$2y$10$hash', '1976-09-25', 'PROFESSOR', 3),
(35, '20730740756', 'Aline', 'Araujo', 'aline.araujo@p.ucb.br', '$2y$10$hash', '1981-12-12', 'PROFESSOR', 2),
(36, '88811144402', 'Juliana', 'Martins Azevedo', 'juliana.azevedo@ucb.br', '$2y$10$hash', '1990-06-25', 'ADMINISTRATIVO', 5),
(37, '99922255513', 'Marcos', 'Vinícius Rocha', 'marcos.rocha@ucb.br', '$2y$10$hash', '1988-01-18', 'ADMINISTRATIVO', 4),
(38, '30140150160', 'Rodrigo', 'Ferreira', 'rodrigo.ferreira@ucb.br', '$2y$10$hash', '1992-03-14', 'ADMINISTRATIVO', 5),
(39, '30240250261', 'Bruna', 'Costa', 'bruna.costa@ucb.br', '$2y$10$hash', '1995-07-07', 'ADMINISTRATIVO', 4),
(40, '30340350362', 'Diego', 'Santos', 'diego.santos@ucb.br', '$2y$10$hash', '1989-10-22', 'ADMINISTRATIVO', 5);

INSERT INTO aluno (id_usuario, matricula, curso, semestre_atual, situacao_academica) VALUES
(1, 'UCB2024101', 'Engenharia de Software', 5, 'ATIVO'),
(2, 'UCB2023255', 'Engenharia de Software', 7, 'ATIVO'),
(3, 'UCB2022198', 'Ciência da Computação', 9, 'TRANCADO'),
(4, 'UCB2025012', 'Engenharia de Software', 2, 'ATIVO'),
(5, 'UCB2021005', 'Engenharia de Software', 8, 'ATIVO'),
(6, 'UCB2023006', 'Ciência da Computação', 5, 'ATIVO'),
(7, 'UCB2022007', 'Engenharia de Software', 7, 'TRANCADO'),
(8, 'UCB2024008', 'Engenharia de Software', 4, 'ATIVO'),
(9, 'UCB2021009', 'Ciência da Computação', 8, 'FORMADO'),
(10, 'UCB2025010', 'Engenharia de Software', 2, 'ATIVO'),
(11, 'UCB2020011', 'Ciência da Computação', 10, 'ATIVO'),
(12, 'UCB2023012', 'Engenharia de Software', 6, 'ATIVO'),
(13, 'UCB2024013', 'Engenharia de Software', 3, 'ATIVO'),
(14, 'UCB2022014', 'Ciência da Computação', 7, 'TRANCADO'),
(15, 'UCB2021015', 'Engenharia de Software', 9, 'ATIVO'),
(16, 'UCB2023016', 'Engenharia de Software', 5, 'ATIVO'),
(17, 'UCB2022017', 'Ciência da Computação', 8, 'ATIVO'),
(18, 'UCB2024018', 'Engenharia de Software', 4, 'ATIVO'),
(19, 'UCB2021019', 'Ciência da Computação', 9, 'ATIVO'),
(20, 'UCB2025020', 'Engenharia de Software', 1, 'ATIVO'),
(21, 'UCB2020021', 'Engenharia de Software', 10, 'FORMADO'),
(22, 'UCB2023022', 'Ciência da Computação', 6, 'ATIVO'),
(23, 'UCB2022023', 'Engenharia de Software', 7, 'ATIVO'),
(24, 'UCB2024024', 'Engenharia de Software', 3, 'ATIVO'),
(25, 'UCB2021025', 'Ciência da Computação', 8, 'ATIVO');

INSERT INTO professor (id_usuario, matricula_docente, titulacao, regime_trabalho) VALUES
(26, 'DOC-8821', 'Doutor', 'Dedicação Exclusiva'),
(27, 'DOC-9102', 'Mestre', '40 horas'),
(28, 'DOC-7412', 'Doutor', 'Dedicação Exclusiva'),
(29, 'DOC-1001', 'Especialista', '20 horas'),
(30, 'DOC-1002', 'Doutor', 'Dedicação Exclusiva'),
(31, 'DOC-1003', 'Mestre', '40 horas'),
(32, 'DOC-1004', 'Doutor', '40 horas'),
(33, 'DOC-1005', 'Mestre', '20 horas'),
(34, 'DOC-1006', 'Especialista', '20 horas'),
(35, 'DOC-1007', 'Doutor', 'Dedicação Exclusiva');

INSERT INTO administrativo (id_usuario, cargo, setor, ramal) VALUES
(36, 'Assistente de Secretaria', 'Secretaria Geral', '4401'),
(37, 'Técnico de Suporte TI', 'Suporte Técnico', '4488'),
(38, 'Coordenador Acadêmico', 'Secretaria Geral', '4402'),
(39, 'Analista de Sistemas', 'Suporte Técnico', '4489'),
(40, 'Auxiliar Administrativo', 'Secretaria Geral', NULL);

-- ==============================================================================
-- 6. TELEFONE_USUARIO (RN09)
-- Múltiplos telefones por utilizador (entidade fraca). Casos com omissão de ramais ou fixos.
-- ==============================================================================
INSERT INTO telefone_usuario (id_usuario, numero_telefone, tipo_telefone) VALUES
(1, '(61) 98877-1122', 'CELULAR'),
(2, '(61) 99233-4455', 'CELULAR'),
(2, '(61) 3356-9900', 'FIXO'),
(3, '(61) 98111-2233', 'CELULAR'),
(26, '(61) 99988-7766', 'CELULAR'),
(26, '(61) 3356-9001', 'RAMAL'),
(36, '(61) 98444-3322', 'CELULAR'),
(36, '(61) 3356-4401', 'RAMAL'),
(37, '(61) 98222-1144', 'CELULAR');

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
(1, '2026-10-01 08:00:00', '2026-10-01 09:40:00', 'REALIZADO', 'Aula prática Lab DB', '2026-09-25 10:00:00', 26, 1, 'LAB-101', 1),
(2, '2026-10-02 14:00:00', '2026-10-02 16:00:00', 'APROVADO', 'Pesquisa Distribuídos', '2026-09-26 11:30:00', 27, 1, 'LAB-102', 2),
(3, '2026-10-05 10:00:00', '2026-10-05 11:00:00', 'SOLICITADO', 'Reunião Colegiado', '2026-09-28 15:45:00', 28, 2, 'SALA-REU1', 3),
(4, '2026-10-10 09:00:00', '2026-10-10 10:30:00', 'CONFIRMADO', 'TCC Grupo AgendaUCB', '2026-09-29 09:15:00', 26, 3, 'AUD-01', 4),
(5, '2026-10-12 14:00:00', '2026-10-12 15:00:00', 'CANCELADO', 'Monitoria (cancelada)', '2026-09-30 08:20:00', 1, 2, 'SALA-205', 5),
(6, '2026-10-15 19:00:00', '2026-10-15 21:00:00', 'SOLICITADO', 'Defesa TCC Noturno', '2026-10-01 14:00:00', 27, 3, 'AUD-01', 4),
(7, '2026-10-16 08:00:00', '2026-10-16 10:00:00', 'REALIZADO', 'Aula de Algoritmos', '2026-10-10 10:00:00', 29, 2, 'SALA-205', 1),
(8, '2026-10-17 14:00:00', '2026-10-17 16:00:00', 'REALIZADO', 'Aula de Redes', '2026-10-11 11:00:00', 30, 1, 'LAB-101', 1),
(9, '2026-10-18 09:00:00', '2026-10-18 11:00:00', 'REALIZADO', 'Reunião Diretoria', '2026-10-12 09:00:00', 38, 2, 'SALA-REU1', 3),
(10, '2026-10-19 19:00:00', '2026-10-19 21:00:00', 'REALIZADO', 'Palestra Segurança', '2026-10-13 14:00:00', 32, 3, 'AUD-01', 1),
(11, '2026-10-20 10:00:00', '2026-10-20 12:00:00', 'REALIZADO', 'Monitoria Cálculo', '2026-10-14 08:00:00', 5, 2, 'SALA-206', 5),
(12, '2026-10-21 14:00:00', '2026-10-21 16:00:00', 'REALIZADO', 'Aula Lab Física', '2026-10-15 10:00:00', 31, 1, 'LAB-102', 1),
(13, '2026-10-22 08:00:00', '2026-10-22 10:00:00', 'REALIZADO', 'Reunião Professores', '2026-10-16 11:00:00', 33, 2, 'SALA-REU1', 3),
(14, '2026-10-23 16:00:00', '2026-10-23 18:00:00', 'REALIZADO', 'Defesa TCC Sistemas', '2026-10-17 09:00:00', 34, 3, 'AUD-01', 4),
(15, '2026-10-24 10:00:00', '2026-10-24 12:00:00', 'REALIZADO', 'Monitoria Programação', '2026-10-18 14:00:00', 6, 2, 'SALA-205', 5),
(16, '2026-10-25 14:00:00', '2026-10-25 16:00:00', 'REALIZADO', 'Aula BD Avançado', '2026-10-19 08:00:00', 26, 1, 'LAB-101', 1),
(17, '2026-10-26 09:00:00', '2026-10-26 11:00:00', 'REALIZADO', 'Reunião Avaliação', '2026-10-20 10:00:00', 39, 2, 'SALA-REU1', 3),
(18, '2026-10-27 19:00:00', '2026-10-27 21:00:00', 'REALIZADO', 'Palestra IA', '2026-10-21 11:00:00', 35, 3, 'AUD-01', 1),
(19, '2026-10-28 10:00:00', '2026-10-28 12:00:00', 'REALIZADO', 'Monitoria UX', '2026-10-22 09:00:00', 7, 2, 'SALA-206', 5),
(20, '2026-10-29 14:00:00', '2026-10-29 16:00:00', 'REALIZADO', 'Pesquisa IoT', '2026-10-23 14:00:00', 27, 1, 'LAB-102', 2),
(21, '2026-11-01 08:00:00', '2026-11-01 10:00:00', 'CONFIRMADO', 'Aula de Algoritmos', '2026-10-25 10:00:00', 29, 2, 'SALA-205', 1),
(22, '2026-11-02 14:00:00', '2026-11-02 16:00:00', 'CONFIRMADO', 'Aula de Redes', '2026-10-26 11:00:00', 30, 1, 'LAB-101', 1),
(23, '2026-11-03 09:00:00', '2026-11-03 11:00:00', 'CONFIRMADO', 'Reunião Diretoria', '2026-10-27 09:00:00', 38, 2, 'SALA-REU1', 3),
(24, '2026-11-04 19:00:00', '2026-11-04 21:00:00', 'CONFIRMADO', 'Palestra Segurança', '2026-10-28 14:00:00', 32, 3, 'AUD-01', 1),
(25, '2026-11-05 10:00:00', '2026-11-05 12:00:00', 'CONFIRMADO', 'Monitoria Cálculo', '2026-10-29 08:00:00', 5, 2, 'SALA-206', 5),
(26, '2026-11-06 14:00:00', '2026-11-06 16:00:00', 'CONFIRMADO', 'Aula Lab Física', '2026-10-30 10:00:00', 31, 1, 'LAB-102', 1),
(27, '2026-11-07 08:00:00', '2026-11-07 10:00:00', 'CONFIRMADO', 'Reunião Professores', '2026-10-31 11:00:00', 33, 2, 'SALA-REU1', 3),
(28, '2026-11-08 16:00:00', '2026-11-08 18:00:00', 'CONFIRMADO', 'Defesa TCC Sistemas', '2026-11-01 09:00:00', 34, 3, 'AUD-01', 4),
(29, '2026-11-09 10:00:00', '2026-11-09 12:00:00', 'CONFIRMADO', 'Monitoria Programação', '2026-11-02 14:00:00', 6, 2, 'SALA-205', 5),
(30, '2026-11-10 14:00:00', '2026-11-10 16:00:00', 'CONFIRMADO', 'Aula BD Avançado', '2026-11-03 08:00:00', 26, 1, 'LAB-101', 1),
(31, '2026-11-11 09:00:00', '2026-11-11 11:00:00', 'APROVADO', 'Reunião Avaliação', '2026-11-04 10:00:00', 39, 2, 'SALA-REU1', 3),
(32, '2026-11-12 19:00:00', '2026-11-12 21:00:00', 'APROVADO', 'Palestra IA', '2026-11-05 11:00:00', 35, 3, 'AUD-01', 1),
(33, '2026-11-13 10:00:00', '2026-11-13 12:00:00', 'APROVADO', 'Monitoria UX', '2026-11-06 09:00:00', 7, 2, 'SALA-206', 5),
(34, '2026-11-14 14:00:00', '2026-11-14 16:00:00', 'APROVADO', 'Pesquisa IoT', '2026-11-07 14:00:00', 27, 1, 'LAB-102', 2),
(35, '2026-11-15 08:00:00', '2026-11-15 10:00:00', 'SOLICITADO', 'Aula de Algoritmos', '2026-11-08 08:00:00', 29, 2, 'SALA-205', 1),
(36, '2026-11-16 14:00:00', '2026-11-16 16:00:00', 'SOLICITADO', 'Aula de Redes', '2026-11-09 10:00:00', 30, 1, 'LAB-101', 1),
(37, '2026-11-17 09:00:00', '2026-11-17 11:00:00', 'SOLICITADO', 'Reunião Diretoria', '2026-11-10 11:00:00', 38, 2, 'SALA-REU1', 3),
(38, '2026-11-18 19:00:00', '2026-11-18 21:00:00', 'SOLICITADO', 'Palestra Segurança', '2026-11-11 09:00:00', 32, 3, 'AUD-01', 1),
(39, '2026-11-19 10:00:00', '2026-11-19 12:00:00', 'SOLICITADO', 'Monitoria Cálculo', '2026-11-12 14:00:00', 5, 2, 'SALA-206', 5),
(40, '2026-11-20 14:00:00', '2026-11-20 16:00:00', 'SOLICITADO', 'Aula Lab Física', '2026-11-13 08:00:00', 31, 1, 'LAB-102', 1);

-- ==============================================================================
-- 9. HISTORICO_STATUS_AGENDAMENTO (RN18)
-- Atributo temporal (situação ao longo do tempo). Casos com mais de um evento de histórico.
-- ==============================================================================
INSERT INTO historico_status_agendamento (id_agendamento, numero_sequencia, situacao_anterior, situacao_nova, data_hora_mudanca, observacao, id_usuario_responsavel) VALUES
(1, 1, NULL, 'SOLICITADO', '2026-09-25 10:00:00', 'Solicitação inicial aberta pelo docente', 26),
(1, 2, 'SOLICITADO', 'APROVADO', '2026-09-25 14:00:00', 'Aprovado automaticamente pelo sistema de aulas', 36),
(1, 3, 'APROVADO', 'CONFIRMADO', '2026-09-26 08:00:00', 'Confirmação de recursos e chaves entregues', 37),
(1, 4, 'CONFIRMADO', 'REALIZADO', '2026-10-01 09:45:00', 'Atividade concluída com sucesso', 26),
(2, 1, NULL, 'SOLICITADO', '2026-09-26 11:30:00', 'Solicitação de laboratório especial', 27),
(2, 2, 'SOLICITADO', 'APROVADO', '2026-09-27 09:00:00', 'Aprovado pela coordenação do departamento', 36),
(3, 1, NULL, 'SOLICITADO', '2026-09-28 15:45:00', 'Solicitação de reunião enviada', 28),
(4, 1, NULL, 'SOLICITADO', '2026-09-29 09:15:00', 'Solicitação de auditorio para TCC', 26),
(4, 2, 'SOLICITADO', 'APROVADO', '2026-09-29 11:00:00', 'Aprovado pela infraestrutura', 36),
(4, 3, 'APROVADO', 'CONFIRMADO', '2026-09-29 16:00:00', 'Equipamentos de som reservados', 37),
(5, 1, NULL, 'SOLICITADO', '2026-09-30 08:20:00', 'Reserva de sala de aula para estudo', 1),
(5, 2, 'SOLICITADO', 'CANCELADO', '2026-09-30 18:00:00', 'Cancelado pelo próprio solicitante', 1),
(6, 1, NULL, 'SOLICITADO', '2026-10-01 14:00:00', 'Solicitação inicial de defesa de TCC', 27),
(7, 1, NULL, 'SOLICITADO', '2026-10-10 10:00:00', 'Solicitação inicial aberta pelo docente', 29), 
(7, 2, 'SOLICITADO', 'APROVADO', '2026-10-10 12:00:00', 'Aprovado automaticamente pelo sistema de aulas', 36), 
(7, 3, 'APROVADO', 'CONFIRMADO', '2026-10-11 09:00:00', 'Confirmação de recursos e chaves entregues', 37), 
(7, 4, 'CONFIRMADO', 'REALIZADO', '2026-10-16 10:05:00', 'Atividade concluída com sucesso', 29),
(8, 1, NULL, 'SOLICITADO', '2026-10-11 11:00:00', 'Solicitação inicial aberta pelo docente', 30), 
(8, 2, 'SOLICITADO', 'APROVADO', '2026-10-11 12:00:00', 'Aprovado automaticamente pelo sistema de aulas', 36), 
(8, 3, 'APROVADO', 'CONFIRMADO', '2026-10-12 09:00:00', 'Confirmação de recursos e chaves entregues', 37), 
(8, 4, 'CONFIRMADO', 'REALIZADO', '2026-10-17 16:05:00', 'Atividade concluída com sucesso', 30),
(9, 1, NULL, 'SOLICITADO', '2026-10-12 09:00:00', 'Solicitação de reunião enviada', 38), 
(9, 2, 'SOLICITADO', 'APROVADO', '2026-10-12 12:00:00', 'Aprovado pela coordenação do departamento', 36), 
(9, 3, 'APROVADO', 'CONFIRMADO', '2026-10-13 09:00:00', 'Confirmação de recursos e chaves entregues', 37), 
(9, 4, 'CONFIRMADO', 'REALIZADO', '2026-10-18 11:05:00', 'Atividade concluída com sucesso', 38),
(10, 1, NULL, 'SOLICITADO', '2026-10-13 14:00:00', 'Solicitação inicial aberta pelo docente', 32), 
(10, 2, 'SOLICITADO', 'APROVADO', '2026-10-13 16:00:00', 'Aprovado automaticamente pelo sistema de aulas', 36), 
(10, 3, 'APROVADO', 'CONFIRMADO', '2026-10-14 09:00:00', 'Confirmação de recursos e chaves entregues', 37), 
(10, 4, 'CONFIRMADO', 'REALIZADO', '2026-10-19 21:05:00', 'Atividade concluída com sucesso', 32),
(11, 1, NULL, 'SOLICITADO', '2026-10-14 08:00:00', 'Reserva de sala de aula para estudo', 5), 
(11, 2, 'SOLICITADO', 'APROVADO', '2026-10-14 12:00:00', 'Aprovado pela coordenação do departamento', 36), 
(11, 3, 'APROVADO', 'CONFIRMADO', '2026-10-15 09:00:00', 'Confirmação de recursos e chaves entregues', 37), 
(11, 4, 'CONFIRMADO', 'REALIZADO', '2026-10-20 12:05:00', 'Atividade concluída com sucesso', 5),
(12, 1, NULL, 'SOLICITADO', '2026-10-15 10:00:00', 'Solicitação inicial aberta pelo docente', 31), 
(12, 2, 'SOLICITADO', 'APROVADO', '2026-10-15 12:00:00', 'Aprovado automaticamente pelo sistema de aulas', 36), 
(12, 3, 'APROVADO', 'CONFIRMADO', '2026-10-16 09:00:00', 'Confirmação de recursos e chaves entregues', 37), 
(12, 4, 'CONFIRMADO', 'REALIZADO', '2026-10-21 16:05:00', 'Atividade concluída com sucesso', 31),
(13, 1, NULL, 'SOLICITADO', '2026-10-16 11:00:00', 'Solicitação de reunião enviada', 33), 
(13, 2, 'SOLICITADO', 'APROVADO', '2026-10-16 12:00:00', 'Aprovado pela coordenação do departamento', 36), 
(13, 3, 'APROVADO', 'CONFIRMADO', '2026-10-17 09:00:00', 'Confirmação de recursos e chaves entregues', 37), 
(13, 4, 'CONFIRMADO', 'REALIZADO', '2026-10-22 10:05:00', 'Atividade concluída com sucesso', 33),
(14, 1, NULL, 'SOLICITADO', '2026-10-17 09:00:00', 'Solicitação inicial de defesa de TCC', 34), 
(14, 2, 'SOLICITADO', 'APROVADO', '2026-10-17 12:00:00', 'Aprovado pela infraestrutura', 36), 
(14, 3, 'APROVADO', 'CONFIRMADO', '2026-10-18 09:00:00', 'Equipamentos de som reservados', 37), 
(14, 4, 'CONFIRMADO', 'REALIZADO', '2026-10-23 18:05:00', 'Atividade concluída com sucesso', 34),
(15, 1, NULL, 'SOLICITADO', '2026-10-18 14:00:00', 'Reserva de sala de aula para estudo', 6), 
(15, 2, 'SOLICITADO', 'APROVADO', '2026-10-18 16:00:00', 'Aprovado pela coordenação do departamento', 36), 
(15, 3, 'APROVADO', 'CONFIRMADO', '2026-10-19 09:00:00', 'Confirmação de recursos e chaves entregues', 37), 
(15, 4, 'CONFIRMADO', 'REALIZADO', '2026-10-24 12:05:00', 'Atividade concluída com sucesso', 6),
(16, 1, NULL, 'SOLICITADO', '2026-10-19 08:00:00', 'Solicitação inicial aberta pelo docente', 26), 
(16, 2, 'SOLICITADO', 'APROVADO', '2026-10-19 12:00:00', 'Aprovado automaticamente pelo sistema de aulas', 36), 
(16, 3, 'APROVADO', 'CONFIRMADO', '2026-10-20 09:00:00', 'Confirmação de recursos e chaves entregues', 37), 
(16, 4, 'CONFIRMADO', 'REALIZADO', '2026-10-25 16:05:00', 'Atividade concluída com sucesso', 26),
(17, 1, NULL, 'SOLICITADO', '2026-10-20 10:00:00', 'Solicitação de reunião enviada', 39), 
(17, 2, 'SOLICITADO', 'APROVADO', '2026-10-20 12:00:00', 'Aprovado pela coordenação do departamento', 36), 
(17, 3, 'APROVADO', 'CONFIRMADO', '2026-10-21 09:00:00', 'Confirmação de recursos e chaves entregues', 37), 
(17, 4, 'CONFIRMADO', 'REALIZADO', '2026-10-26 11:05:00', 'Atividade concluída com sucesso', 39),
(18, 1, NULL, 'SOLICITADO', '2026-10-21 11:00:00', 'Solicitação inicial aberta pelo docente', 35), 
(18, 2, 'SOLICITADO', 'APROVADO', '2026-10-21 12:00:00', 'Aprovado automaticamente pelo sistema de aulas', 36), 
(18, 3, 'APROVADO', 'CONFIRMADO', '2026-10-22 09:00:00', 'Confirmação de recursos e chaves entregues', 37), 
(18, 4, 'CONFIRMADO', 'REALIZADO', '2026-10-27 21:05:00', 'Atividade concluída com sucesso', 35),
(19, 1, NULL, 'SOLICITADO', '2026-10-22 09:00:00', 'Reserva de sala de aula para estudo', 7), 
(19, 2, 'SOLICITADO', 'APROVADO', '2026-10-22 12:00:00', 'Aprovado pela coordenação do departamento', 36), 
(19, 3, 'APROVADO', 'CONFIRMADO', '2026-10-23 09:00:00', 'Confirmação de recursos e chaves entregues', 37), 
(19, 4, 'CONFIRMADO', 'REALIZADO', '2026-10-28 12:05:00', 'Atividade concluída com sucesso', 7),
(20, 1, NULL, 'SOLICITADO', '2026-10-23 14:00:00', 'Solicitação de laboratório especial', 27), 
(20, 2, 'SOLICITADO', 'APROVADO', '2026-10-23 16:00:00', 'Aprovado pela coordenação do departamento', 36), 
(20, 3, 'APROVADO', 'CONFIRMADO', '2026-10-24 09:00:00', 'Confirmação de recursos e chaves entregues', 37), 
(20, 4, 'CONFIRMADO', 'REALIZADO', '2026-10-29 16:05:00', 'Atividade concluída com sucesso', 27),
(21, 1, NULL, 'SOLICITADO', '2026-10-25 10:00:00', 'Solicitação inicial aberta pelo docente', 29), 
(21, 2, 'SOLICITADO', 'APROVADO', '2026-10-25 12:00:00', 'Aprovado automaticamente pelo sistema de aulas', 36), 
(21, 3, 'APROVADO', 'CONFIRMADO', '2026-10-26 09:00:00', 'Confirmação de recursos e chaves entregues', 37),
(22, 1, NULL, 'SOLICITADO', '2026-10-26 11:00:00', 'Solicitação inicial aberta pelo docente', 30), 
(22, 2, 'SOLICITADO', 'APROVADO', '2026-10-26 12:00:00', 'Aprovado automaticamente pelo sistema de aulas', 36), 
(22, 3, 'APROVADO', 'CONFIRMADO', '2026-10-27 09:00:00', 'Confirmação de recursos e chaves entregues', 37),
(23, 1, NULL, 'SOLICITADO', '2026-10-27 09:00:00', 'Solicitação de reunião enviada', 38), 
(23, 2, 'SOLICITADO', 'APROVADO', '2026-10-27 12:00:00', 'Aprovado pela coordenação do departamento', 36), 
(23, 3, 'APROVADO', 'CONFIRMADO', '2026-10-28 09:00:00', 'Confirmação de recursos e chaves entregues', 37),
(24, 1, NULL, 'SOLICITADO', '2026-10-28 14:00:00', 'Solicitação inicial aberta pelo docente', 32), 
(24, 2, 'SOLICITADO', 'APROVADO', '2026-10-28 16:00:00', 'Aprovado automaticamente pelo sistema de aulas', 36), 
(24, 3, 'APROVADO', 'CONFIRMADO', '2026-10-29 09:00:00', 'Confirmação de recursos e chaves entregues', 37),
(25, 1, NULL, 'SOLICITADO', '2026-10-29 08:00:00', 'Reserva de sala de aula para estudo', 5),  
(25, 2, 'SOLICITADO', 'APROVADO', '2026-10-29 12:00:00', 'Aprovado pela coordenação do departamento', 36), 
(25, 3, 'APROVADO', 'CONFIRMADO', '2026-10-30 09:00:00', 'Confirmação de recursos e chaves entregues', 37),
(26, 1, NULL, 'SOLICITADO', '2026-10-30 10:00:00', 'Solicitação inicial aberta pelo docente', 31), 
(26, 2, 'SOLICITADO', 'APROVADO', '2026-10-30 12:00:00', 'Aprovado automaticamente pelo sistema de aulas', 36), 
(26, 3, 'APROVADO', 'CONFIRMADO', '2026-10-31 09:00:00', 'Confirmação de recursos e chaves entregues', 37),
(27, 1, NULL, 'SOLICITADO', '2026-10-31 11:00:00', 'Solicitação de reunião enviada', 33), 
(27, 2, 'SOLICITADO', 'APROVADO', '2026-10-31 12:00:00', 'Aprovado pela coordenação do departamento', 36), 
(27, 3, 'APROVADO', 'CONFIRMADO', '2026-11-01 09:00:00', 'Confirmação de recursos e chaves entregues', 37),
(28, 1, NULL, 'SOLICITADO', '2026-11-01 09:00:00', 'Solicitação inicial de defesa de TCC', 34), 
(28, 2, 'SOLICITADO', 'APROVADO', '2026-11-01 12:00:00', 'Aprovado pela infraestrutura', 36), 
(28, 3, 'APROVADO', 'CONFIRMADO', '2026-11-02 09:00:00', 'Equipamentos de som reservados', 37),
(29, 1, NULL, 'SOLICITADO', '2026-11-02 14:00:00', 'Reserva de sala de aula para estudo', 6),  
(29, 2, 'SOLICITADO', 'APROVADO', '2026-11-02 16:00:00', 'Aprovado pela coordenação do departamento', 36), 
(29, 3, 'APROVADO', 'CONFIRMADO', '2026-11-03 09:00:00', 'Confirmação de recursos e chaves entregues', 37),
(30, 1, NULL, 'SOLICITADO', '2026-11-03 08:00:00', 'Solicitação inicial aberta pelo docente', 26), 
(30, 2, 'SOLICITADO', 'APROVADO', '2026-11-03 12:00:00', 'Aprovado automaticamente pelo sistema de aulas', 36), 
(30, 3, 'APROVADO', 'CONFIRMADO', '2026-11-04 09:00:00', 'Confirmação de recursos e chaves entregues', 37),
(31, 1, NULL, 'SOLICITADO', '2026-11-04 10:00:00', 'Solicitação de reunião enviada', 39), 
(31, 2, 'SOLICITADO', 'APROVADO', '2026-11-04 12:00:00', 'Aprovado pela coordenação do departamento', 36),
(32, 1, NULL, 'SOLICITADO', '2026-11-05 11:00:00', 'Solicitação inicial aberta pelo docente', 35), 
(32, 2, 'SOLICITADO', 'APROVADO', '2026-11-05 12:00:00', 'Aprovado automaticamente pelo sistema de aulas', 36),
(33, 1, NULL, 'SOLICITADO', '2026-11-06 09:00:00', 'Reserva de sala de aula para estudo', 7),  
(33, 2, 'SOLICITADO', 'APROVADO', '2026-11-06 12:00:00', 'Aprovado pela coordenação do departamento', 36),
(34, 1, NULL, 'SOLICITADO', '2026-11-07 14:00:00', 'Solicitação de laboratório especial', 27), 
(34, 2, 'SOLICITADO', 'APROVADO', '2026-11-07 16:00:00', 'Aprovado pela coordenação do departamento', 36),
(35, 1, NULL, 'SOLICITADO', '2026-11-08 08:00:00', 'Solicitação inicial aberta pelo docente', 29),
(36, 1, NULL, 'SOLICITADO', '2026-11-09 10:00:00', 'Solicitação inicial aberta pelo docente', 30),
(37, 1, NULL, 'SOLICITADO', '2026-11-10 11:00:00', 'Solicitação de reunião enviada', 38),
(38, 1, NULL, 'SOLICITADO', '2026-11-11 09:00:00', 'Solicitação inicial aberta pelo docente', 32),
(39, 1, NULL, 'SOLICITADO', '2026-11-12 14:00:00', 'Reserva de sala de aula para estudo', 5),
(40, 1, NULL, 'SOLICITADO', '2026-11-13 08:00:00', 'Solicitação inicial aberta pelo docente', 31);

-- ==============================================================================
-- 10. AGENDAMENTO_RECURSO (RN20)
-- Associação N:N entre agendamento e recurso com atributos próprios (quantidade e devolução).
-- ==============================================================================
INSERT INTO agendamento_recurso (id_agendamento, id_recurso, quantidade_reservada, situacao_devolucao) VALUES
(1, 1, 1, 'DEVOLVIDO'), (1, 2, 1, 'DEVOLVIDO'), (2, 1, 1, 'PENDENTE'), (2, 5, 2, 'PENDENTE'),
(4, 4, 2, 'DEVOLVIDO'), (4, 1, 1, 'DEVOLVIDO'), (6, 4, 1, NULL),
(7, 1, 1, 'DEVOLVIDO'), (8, 2, 1, 'DEVOLVIDO'), (10, 4, 1, 'DEVOLVIDO'), (12, 5, 1, 'DEVOLVIDO');

-- ==============================================================================
-- 11. PARTICIPACAO_AGENDAMENTO (RN21)
-- Associação N:N entre agendamento e usuário com papéis e status de confirmação.
-- ==============================================================================
INSERT INTO participacao_agendamento (id_agendamento, id_usuario, papel_participante, status_confirmacao, data_confirmacao) VALUES
(1, 1, 'RESPONSAVEL_TECNICO', 'CONFIRMADO', '2026-09-25 10:30:00'),
(1, 2, 'OBSERVADOR', 'CONFIRMADO', '2026-09-25 11:00:00'),
(2, 27, 'RESPONSAVEL_TECNICO', 'CONFIRMADO', '2026-09-26 12:00:00'),
(2, 3, 'CONVIDADO', 'PENDENTE', NULL),
(3, 28, 'RESPONSAVEL_TECNICO', 'CONFIRMADO', '2026-09-28 16:00:00'),
(3, 36, 'CONVIDADO', 'CONFIRMADO', '2026-09-28 17:00:00'),
(4, 26, 'RESPONSAVEL_TECNICO', 'CONFIRMADO', '2026-09-29 09:30:00'),
(4, 1, 'CONVIDADO', 'CONFIRMADO', '2026-09-29 10:00:00');