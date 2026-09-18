-- ==============================================================================
-- AgendaUCB — Script de Consultas de Verificação (03_consultas.sql)
-- Atualizado para o Modelo Lógico de Múltiplas Tabelas de Especialização.
-- ==============================================================================

USE agendaucb;

-- ==============================================================================
-- CATEGORIA 1: CONSULTAS BÁSICAS (Projeção, seleção com WHERE, ordenação, LIKE, BETWEEN, IN, NULL)
-- ==============================================================================

-- Q1: Listar todos os alunos ativos da Engenharia de Software, ordenados pelo nome (Exige JOIN com a nova tabela aluno).
-- Categoria: Básica (Seleção, Ordenação, Junção Simples)
SELECT u.nome, u.sobrenome, u.email_institucional, a.semestre_atual
FROM usuario u
JOIN aluno a ON u.id_usuario = a.id_usuario
WHERE u.tipo_usuario = 'ALUNO' 
  AND a.curso = 'Engenharia de Software' 
  AND a.situacao_academica = 'ATIVO'
ORDER BY u.nome ASC;

-- Q2: Encontrar usuários cujo nome ou sobrenome contenham a letra 'a' (case-insensitive), nascidos entre 1980 e 1995.
-- Categoria: Básica (LIKE, BETWEEN)
SELECT id_usuario, nome, sobrenome, data_nascimento, tipo_usuario
FROM usuario
WHERE (nome LIKE '%a%' OR sobrenome LIKE '%a%')
  AND data_nascimento BETWEEN '1980-01-01' AND '1995-12-31'
ORDER BY data_nascimento DESC;

-- Q3: Listar todos os agendamentos realizados ou confirmados que ocorreram/ocorrerão no mês de outubro de 2026.
-- Categoria: Básica (BETWEEN em datas, IN)
SELECT id_agendamento, data_hora_inicio, data_hora_fim, situacao_atual, id_predio, numero_sala
FROM agendamento
WHERE situacao_atual IN ('CONFIRMADO', 'REALIZADO')
  AND data_hora_inicio BETWEEN '2026-10-01 00:00:00' AND '2026-10-31 23:59:59'
ORDER BY data_hora_inicio ASC;

-- Q4: Listar todos os telefones de usuários que não possuem ramal cadastrado (tipo telefone DIFERENTE de RAMAL ou nulo).
-- Categoria: Básica (Tratamento de NULL / operadores lógicos)
SELECT id_usuario, numero_telefone, tipo_telefone
FROM telefone_usuario
WHERE tipo_telefone != 'RAMAL' OR tipo_telefone IS NULL
ORDER BY id_usuario;

-- Q5: Listar salas de aula ou laboratórios que possuem capacidade superior a 35 lugares e contam com projetor.
-- Categoria: Básica (Seleção com operadores lógicos e IN)
SELECT numero_sala, id_predio, capacidade, tipo_sala
FROM sala
WHERE tipo_sala IN ('SALA_DE_AULA', 'LABORATORIO')
  AND capacidade > 35
  AND possui_projetor = 1
ORDER BY capacidade DESC;


-- ==============================================================================
-- CATEGORIA 2: JUNÇÕES E AGREGAÇÃO (Pelo menos 1 com 3 tabelas, 1 com LEFT JOIN, 1 com GROUP BY e HAVING)
-- ==============================================================================

-- Q6: Relacionar cada agendamento com o nome completo do solicitante e a descrição do tipo de agendamento.
-- Categoria: Junções e Agregação (3 tabelas)
SELECT a.id_agendamento, 
       CONCAT(u.nome, ' ', u.sobrenome) AS solicitante, 
       t.descricao AS tipo_agendamento, 
       a.data_hora_inicio, 
       a.situacao_atual
FROM agendamento a
JOIN usuario u ON a.id_usuario_solicitante = u.id_usuario
JOIN tipo_agendamento t ON a.id_tipo = t.id_tipo
ORDER BY a.data_hora_inicio;

-- Q7: Listar todos os usuários, os seus telefones e a sua informação específica (curso, titulação ou cargo) dependendo do subtipo.
-- Categoria: Junções e Agregação (Múltiplos LEFT JOINs para lidar com a especialização dividida e COALESCE)
SELECT u.id_usuario, u.nome, u.tipo_usuario, 
       COALESCE(al.curso, p.titulacao, ad.cargo) AS vinculo_especifico,
       t.numero_telefone
FROM usuario u
LEFT JOIN telefone_usuario t ON u.id_usuario = t.id_usuario
LEFT JOIN aluno al ON u.id_usuario = al.id_usuario
LEFT JOIN professor p ON u.id_usuario = p.id_usuario
LEFT JOIN administrativo ad ON u.id_usuario = ad.id_usuario
ORDER BY u.id_usuario;

-- Q8: Contar quantos agendamentos foram registados por cada situação atual, exibindo apenas as situações com mais de 1 agendamento.
-- Categoria: Junções e Agregação (GROUP BY e HAVING)
SELECT situacao_atual, COUNT(*) AS total_agendamentos
FROM agendamento
GROUP BY situacao_atual
HAVING COUNT(*) > 1
ORDER BY total_agendamentos DESC;

-- Q9: Calcular a capacidade total e a capacidade média das salas agrupadas por cada tipo de sala existente.
-- Categoria: Junções e Agregação (Agregação simples com GROUP BY)
SELECT tipo_sala, 
       COUNT(*) AS total_salas, 
       SUM(capacidade) AS capacidade_total, 
       ROUND(AVG(capacidade), 2) AS capacidade_media
FROM sala
GROUP BY tipo_sala
ORDER BY capacidade_total DESC;

-- Q10: Listar os prédios e a quantidade de salas associadas a cada um deles.
-- Categoria: Junções e Agregação (JOIN com GROUP BY)
SELECT p.nome AS predio, p.logradouro, COUNT(s.numero_sala) AS quantidade_salas
FROM predio p
JOIN sala s ON p.id_predio = s.id_predio
GROUP BY p.id_predio, p.nome, p.logradouro
ORDER BY quantidade_salas DESC;


-- ==============================================================================
-- CATEGORIA 3: AVANÇADAS (Subconsulta correlacionada, EXISTS, e perguntas de negócio não triviais)
-- ==============================================================================

-- Q11: Identificar usuários cuja data de nascimento é anterior à data de nascimento média de todos os usuários registados.
-- Categoria: Avançada (Subconsulta correlacionada / escalar)
SELECT id_usuario, nome, sobrenome, data_nascimento, tipo_usuario
FROM usuario
WHERE data_nascimento < (
    SELECT AVG(CAST(data_nascimento AS DATE)) FROM usuario
)
ORDER BY data_nascimento ASC;

-- Q12: Listar prédios que possuem pelo menos uma sala com capacidade superior a 100 lugares.
-- Categoria: Avançada (EXISTS)
SELECT p.id_predio, p.nome, p.logradouro
FROM predio p
WHERE EXISTS (
    SELECT 1 
    FROM sala s 
    WHERE s.id_predio = p.id_predio 
      AND s.capacidade > 100
);

-- Q13: Encontrar o histórico de mudanças de status mais recente de cada agendamento.
-- Categoria: Avançada (Subconsulta não trivial / agregação temporal)
SELECT h.id_agendamento, h.numero_sequencia, h.situacao_nova, h.data_hora_mudanca, h.observacao
FROM historico_status_agendamento h
JOIN (
    SELECT id_agendamento, MAX(numero_sequencia) as max_seq
    FROM historico_status_agendamento
    GROUP BY id_agendamento
) latest ON h.id_agendamento = latest.id_agendamento AND h.numero_sequencia = latest.max_seq
ORDER BY h.data_hora_mudanca DESC;

-- Q14: Listar os recursos que já foram reservados em quantidade total superior a 1 unidade somando todos os seus agendamentos.
-- Categoria: Avançada (Agregação complexa com Junção N:N e HAVING)
SELECT r.id_recurso, r.nome AS recurso, r.tipo_recurso, SUM(ar.quantidade_reservada) AS total_reservado
FROM recurso r
JOIN agendamento_recurso ar ON r.id_recurso = ar.id_recurso
GROUP BY r.id_recurso, r.nome, r.tipo_recurso
HAVING SUM(ar.quantidade_reservada) > 1
ORDER BY total_reservado DESC;

-- Q15: Identificar os departamentos que possuem colaboradores com idade superior à média de idade de todos os utilizadores da instituição.
-- Categoria: Avançada (Subconsulta complexa com agrupamento e junção institucional)
SELECT DISTINCT d.id_departamento, d.nome AS departamento, d.sigla
FROM departamento d
JOIN usuario u ON d.id_departamento = u.id_departamento
WHERE TIMESTAMPDIFF(YEAR, u.data_nascimento, CURDATE()) > (
    SELECT AVG(TIMESTAMPDIFF(YEAR, data_nascimento, CURDATE())) 
    FROM usuario
)
ORDER BY d.nome;