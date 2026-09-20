# AgendaUCB
Projeto acadêmico desenvolvido para a disciplina de Laboratório de Banco de Dados (2026/2) da Universidade Católica de Brasília (UCB).

## Tema e Escopo
O **AgendaUCB** é uma plataforma criada para simplificar e centralizar o agendamento de espaços físicos e recursos materiais no campus universitário. O sistema atende a toda a comunidade acadêmica, dividida em perfis especializados (discentes, docentes e gestão departamental).

Através da plataforma, é possível:
- **Reservar Salas e Laboratórios:** Agendar espaços em diferentes prédios para eventos e atividades acadêmicas.
- **Solicitar Equipamentos:** Incluir recursos extras na reserva (como projetores e notebooks), com controle de estoque e devolução.
- **Gerenciar Participantes e Inscrições:** Controlar vagas, presença e emissão de certificados com chave de autenticação única.
- **Fluxo de Aprovação e Rastreabilidade:** Ciclo de vida da reserva com histórico temporal auditável de mudanças de status.

## Integrantes da Equipe
- Alessandro de Araújo Magalhães — Modelador de Dados / DDL
- Átila Batista Martins — Administrador de Banco / Carga
- Carlos André Serpa Nataniel — Apresentador / Documentação e Regras de Negócio
- Ângelo Gabriel Cirqueira Assunção Paraguai — Apresentador / Documentação e Regras de Negócio
-Daniel Pereira de Amorim - Consultas / Verificação de Dados

## Tecnologias Utilizadas
- SGBD: MySQL 8.0+
- Codificação de Caracteres: utf8mb4
- Ferramenta de Modelagem: MySQL Workbench / draw.io

## Estrutura do Repositório
- `docs/`: Documentação textual, modelos conceituais/lógicos, dicionário de dados e roteiros em PDF/Markdown.
- `sql/`:
  - `01_ddl.sql`: Script físico de criação do esquema, tabelas, restrições nomeadas e índices.
  - `02_carga.sql`: Script com a massa de dados sintéticos para testes e casos de contorno.
  - `03_consultas.sql`: Script com as 15 consultas de verificação e validação de negócio.

## Como Executar o Banco de Dados
1. Abra o ambiente MySQL (Terminal ou MySQL Workbench).
2. Execute o script `sql/01_ddl.sql` para criar o banco de dados `agenda_ucb` e suas estruturas em base limpa.
3. Execute o script `sql/02_carga.sql` para popular as tabelas com os dados de teste.
4. Execute as consultas presentes em `sql/03_consultas.sql` para validar o funcionamento das buscas.

---

## 📋 Rastreabilidade das Regras de Negócio (RN01 a RN20)

| Regra | Descrição Sumária | Implementação no Banco / Aplicação |
| :--- | :--- | :--- |
| **RN01** | E-mail institucional obrigatório (@ucb.br ou @p.ucb.br) | Restrição `ck_usuario_email` na tabela `usuario` |
| **RN02** | CPF único composto por 11 dígitos numéricos | Restrições `uq_usuario_cpf` e `ck_usuario_cpf` |
| **RN03** | Discente com matrícula ativa e curso vinculado | Chave primária/estrangeira na tabela `discente` |
| **RN04** | Docente com código funcional e departamento | Tabela `docente` com FK para `departamento` |
| **RN05** | Capacidade máxima de lotação do espaço físico | Restrição `ck_espaco_capacidade` e validação relacional |
| **RN06** | Horário de término posterior ao horário de início | Restrição `ck_reserva_horarios` na tabela `reserva` |
| **RN07** | Não sobreposição de reservas no mesmo espaço | Índice composto `idx_reserva_data_hora` e camada transacional |
| **RN08** | Solicitação de reserva com antecedência mínima | Validação na camada de regras de negócio / aplicação |
| **RN09** | Transição de status controlada (`SOLICITADA` a `CANCELADA`) | Domínio `ENUM` na coluna `status` da tabela `reserva` |
| **RN10** | Registro histórico auditável de mudanças de status | Tabela temporal `historico_status_reserva` |
| **RN11** | Responsável pelo evento deve ser docente cadastrado | Restrição `fk_evento_docente` apontando para `docente` |
| **RN12** | Data final do evento maior ou igual à data inicial | Restrição `ck_evento_datas` na tabela `evento` |
| **RN13** | Atividade vinculada a um evento acadêmico | Restrição `fk_atividade_evento` (`ON DELETE CASCADE`) |
| **RN14** | Pré-requisito lógico entre atividades acadêmicas | Autorrelacionamento via `fk_atividade_prerequisito` |
| **RN15** | Limite de vagas ofertadas por atividade | Atributo `vagas_totais` e controle via `COUNT` em `inscricao` |
| **RN16** | Inscrição única por discente em cada atividade | Chave primária composta `pk_inscricao (id_usuario, id_atividade)` |
| **RN17** | Alocação de recursos limitada ao estoque | Restrição `ck_alocacao_qtd` e validação relacional |
| **RN18** | Devolução com avaria registrada para auditoria | Atributo booleano `devolvido_sem_avaria` em `alocacao_recurso` |
| **RN19** | Certificado emitido apenas com presença ≥ 75% | Validação lógica e entidade fraca `certificado` |
| **RN20** | Código de autenticação de certificado único | Restrição `uq_certificado_codigo` na tabela `certificado` |
