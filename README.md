AgendaUcb
Projeto acadêmico desenvolvido para a disciplina de Laboratório de Banco de Dados (2026/2) da Universidade Católica de Brasília (UCB).

Tema e Escopo
O AgendaUCB é uma plataforma que centraliza o agendamento de espaços físicos e recursos materiais
dentro do campus universitário

Integrantes da Equipe
Alessandro de Araújo Magalhães - alessandro.magalhaes@a.ucb.br

Tecnologias Utilizadas
SGBD: MySQL 8.0+
Codificação de Caracteres: utf8mb4
Ferramenta de Modelagem: [Ex: MySQL Workbench / draw.io]

Estrutura do Repositório

docs/: Documentação textual, modelos conceituais/lógicos e dicionário de dados em PDF.
sql/:
01_ddl.sql: Script de criação das tabelas, triggers e views do banco.
02_carga.sql: Script com a massa de dados para testes.
03_consultas.sql: Script com as 15 consultas de verificação.

Como Executar o Banco de Dados

Abra o ambiente MySQL (Terminal ou MySQL Workbench).
Execute o script sql/01_ddl.sql para criar o banco de dados sgatm e suas estruturas.
Execute o script sql/02_carga.sql para popular as tabelas com os dados de teste.
Execute as consultas presentes em sql/03_consultas.sql para validar o funcionamento das buscas.
