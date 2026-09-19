# AgendaUcb
Projeto acadêmico desenvolvido para a disciplina de Laboratório de Banco de Dados (2026/2) da Universidade Católica de Brasília (UCB).

## Tema e Escopo
O **AgendaUCB** é uma plataforma criada para simplificar e centralizar o agendamento de espaços físicos e recursos materiais no campus universitário. O sistema atende a toda a comunidade acadêmica, dividida em três perfis principais: **alunos, professores e equipe administrativa** (que estão organizados em departamentos hierárquicos).

Através da plataforma, é possível:

- **Reservar Salas e Laboratórios:** Agendar espaços em diferentes prédios para finalidades específicas (como reuniões de colegiado, monitorias, defesas de TCC, etc.).
- **Solicitar Equipamentos:** Incluir recursos extras na reserva (como projetores e notebooks), com o sistema controlando automaticamente o estoque para não ultrapassar a quantidade disponível.
- **Gerenciar Participantes:** Adicionar convidados aos agendamentos, permitindo que eles confirmem presença.
- **Fluxo de Aprovação:** Lidar com tipos de reserva que exigem autorização prévia de um responsável antes de serem confirmadas.

Para garantir a organização do campus, o sistema **impede conflitos de horários** (evitando reservas duplicadas para a mesma sala ou equipamento) e mantém um **histórico de auditoria completo**. Cada agendamento passa por um ciclo de vida claro (solicitado, aprovado, confirmado, realizado ou cancelado), e a plataforma registra exatamente quem solicitou, quem aprovou e quando cada mudança de status ocorreu.

## Integrantes da Equipe
- Alessandro de Araújo Magalhães - alessandro.magalhaes@a.ucb.br
- Átila Batista Martins - atila.martins@a.ucb.br
- 
- 
- 

## Tecnologias Utilizadas
- SGBD: MySQL 8.0+
- Codificação de Caracteres: utf8mb4
- Ferramenta de Modelagem: [Ex: MySQL Workbench / draw.io]

## Estrutura do Repositório
- docs/: Documentação textual, modelos conceituais/lógicos e dicionário de dados em PDF.
- sql/:
  - 01_ddl.sql: Script de criação das tabelas, triggers e views do banco.
  - 02_carga.sql: Script com a massa de dados para testes.
  - 03_consultas.sql: Script com as 15 consultas de verificação.

## Como Executar o Banco de Dados
1. Abra o ambiente MySQL (Terminal ou MySQL Workbench).
2. Execute o script sql/01_ddl.sql para criar o banco de dados AgendaUcb e suas estruturas.
3. Execute o script sql/02_carga.sql para popular as tabelas com os dados de teste.
4. Execute as consultas presentes em sql/03_consultas.sql para validar o funcionamento das buscas.