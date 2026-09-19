-- ==============================================================================
-- AgendaUCB — Script físico (DDL)
-- Mapeamento da especialização: Tabelas Múltiplas (aluno, professor, administrativo)
-- ==============================================================================

DROP DATABASE IF EXISTS agendaucb;
CREATE DATABASE agendaucb;
USE agendaucb;

-- RN05: Um departamento pode estar subordinado a, no máximo, um departamento superior.
CREATE TABLE departamento (
    id_departamento INT AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    sigla VARCHAR(10) NOT NULL,
    data_criacao DATE NOT NULL,
    id_departamento_pai INT NULL,
    CONSTRAINT pk_departamento PRIMARY KEY (id_departamento),
    CONSTRAINT uq_departamento_sigla UNIQUE (sigla),
    CONSTRAINT fk_departamento_pai FOREIGN KEY (id_departamento_pai) REFERENCES departamento(id_departamento) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- RN01: Todo usuário do sistema deve ser classificado em exatamente um dos tipos: Aluno, Professor ou Administrativo.
-- RN02: O CPF de cada usuário é único no sistema.
-- RN03: O e-mail institucional de cada usuário é único no sistema.
-- RN04: Cada usuário está vinculado a exatamente um departamento.
CREATE TABLE usuario (
    id_usuario INT AUTO_INCREMENT,
    cpf VARCHAR(11) NOT NULL,
    nome VARCHAR(60) NOT NULL,
    sobrenome VARCHAR(100) NOT NULL,
    email_institucional VARCHAR(120) NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    data_nascimento DATE NOT NULL,
    tipo_usuario ENUM('ALUNO', 'PROFESSOR', 'ADMINISTRATIVO') NOT NULL,
    id_departamento INT NOT NULL,
    CONSTRAINT pk_usuario PRIMARY KEY (id_usuario),
    CONSTRAINT uq_usuario_cpf UNIQUE (cpf),
    CONSTRAINT uq_usuario_email UNIQUE (email_institucional),
    CONSTRAINT fk_usuario_depto FOREIGN KEY (id_departamento) REFERENCES departamento(id_departamento) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- RN19: A data de nascimento de um usuário não pode ser futura.
DELIMITER //
CREATE TRIGGER trg_ck_usuario_data_nasc_ins BEFORE INSERT ON usuario FOR EACH ROW
BEGIN
    IF NEW.data_nascimento > CURRENT_DATE THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Erro de Restrição (RN19): A data de nascimento não pode ser futura.';
    END IF;
END; //
CREATE TRIGGER trg_ck_usuario_data_nasc_upd BEFORE UPDATE ON usuario FOR EACH ROW
BEGIN
    IF NEW.data_nascimento > CURRENT_DATE THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Erro de Restrição (RN19): A data de nascimento não pode ser futura.';
    END IF;
END; //
DELIMITER ;

CREATE TABLE telefone_usuario (
    id_usuario INT NOT NULL,
    numero_telefone VARCHAR(20) NOT NULL,
    tipo_telefone ENUM('CELULAR', 'FIXO', 'RAMAL') NULL,
    CONSTRAINT pk_telefone PRIMARY KEY (id_usuario, numero_telefone),
    CONSTRAINT fk_telefone_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE ON UPDATE CASCADE
);

-- RN16: A matrícula de um aluno é única no sistema.
-- Tabelas de Especialização (Mapeamento em Múltiplas Tabelas)
CREATE TABLE aluno (
    id_usuario INT NOT NULL,
    matricula VARCHAR(15) NOT NULL,
    curso VARCHAR(80) NOT NULL,
    semestre_atual INT NOT NULL,
    situacao_academica ENUM('ATIVO', 'TRANCADO', 'FORMADO') NOT NULL,
    CONSTRAINT pk_aluno PRIMARY KEY (id_usuario),
    CONSTRAINT uq_aluno_matricula UNIQUE (matricula),
    CONSTRAINT fk_aluno_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE professor (
    id_usuario INT NOT NULL,
    matricula_docente VARCHAR(15) NOT NULL,
    titulacao VARCHAR(40) NOT NULL,
    regime_trabalho ENUM('Dedicação Exclusiva', '40 horas', '20 horas') NOT NULL,
    CONSTRAINT pk_professor PRIMARY KEY (id_usuario),
    CONSTRAINT uq_professor_mat_doc UNIQUE (matricula_docente),
    CONSTRAINT fk_professor_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE administrativo (
    id_usuario INT NOT NULL,
    cargo VARCHAR(60) NOT NULL,
    setor VARCHAR(60) NOT NULL,
    ramal VARCHAR(6) NULL,
    CONSTRAINT pk_administrativo PRIMARY KEY (id_usuario),
    CONSTRAINT fk_admin_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE predio (
    id_predio INT AUTO_INCREMENT,
    nome VARCHAR(60) NOT NULL,
    logradouro VARCHAR(120) NOT NULL,
    cep VARCHAR(8) NULL,
    quantidade_andares INT NOT NULL,
    CONSTRAINT pk_predio PRIMARY KEY (id_predio),
    CONSTRAINT ck_predio_andares CHECK (quantidade_andares > 0)
);

-- RN07: O número de uma sala é único dentro do prédio ao qual pertence, podendo se repetir em prédios diferentes.
CREATE TABLE sala (
    id_predio INT NOT NULL,
    numero_sala VARCHAR(10) NOT NULL,
    capacidade INT NOT NULL,
    tipo_sala ENUM('SALA_DE_AULA', 'LABORATORIO', 'AUDITORIO', 'REUNIAO') NOT NULL,
    possui_projetor BOOLEAN NOT NULL,
    CONSTRAINT pk_sala PRIMARY KEY (id_predio, numero_sala),
    CONSTRAINT fk_sala_predio FOREIGN KEY (id_predio) REFERENCES predio(id_predio) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT ck_sala_capacidade CHECK (capacidade > 0)
);

-- RN13: Tipos de agendamento.
CREATE TABLE tipo_agendamento (
    id_tipo INT AUTO_INCREMENT,
    descricao VARCHAR(80) NOT NULL,
    duracao_padrao_minutos INT NULL,
    requer_aprovacao BOOLEAN NOT NULL,
    CONSTRAINT pk_tipo_agendamento PRIMARY KEY (id_tipo),
    CONSTRAINT uq_tipo_agendamento_descricao UNIQUE (descricao)
);

-- RN14: Recursos.
CREATE TABLE recurso (
    id_recurso INT AUTO_INCREMENT,
    nome VARCHAR(80) NOT NULL,
    tipo_recurso ENUM('EQUIPAMENTO', 'CONSUMO', 'CHAVE') NOT NULL,
    patrimonio VARCHAR(30) NULL,
    quantidade_total_disponivel INT NOT NULL,
    CONSTRAINT pk_recurso PRIMARY KEY (id_recurso),
    CONSTRAINT ck_recurso_qtd CHECK (quantidade_total_disponivel >= 0)
);

-- RN08: Em todo agendamento, a data/hora de início deve ser estritamente anterior à data/hora de término.
-- RN09: Todo agendamento possui exatamente um usuário solicitante.
-- RN10: Todo agendamento está associado a exatamente uma sala e a exatamente um tipo de agenda
CREATE TABLE agendamento (
    id_agendamento INT AUTO_INCREMENT,
    data_hora_inicio DATETIME NOT NULL,
    data_hora_fim DATETIME NOT NULL,
    situacao_atual ENUM('SOLICITADO', 'APROVADO', 'CONFIRMADO', 'REALIZADO', 'CANCELADO') NOT NULL,
    motivo_descricao VARCHAR(250) NULL,
    data_criacao DATETIME NOT NULL,
    id_usuario_solicitante INT NOT NULL,
    id_predio INT NOT NULL,
    numero_sala VARCHAR(10) NOT NULL,
    id_tipo INT NOT NULL,
    CONSTRAINT pk_agendamento PRIMARY KEY (id_agendamento),
    CONSTRAINT fk_agendamento_usuario FOREIGN KEY (id_usuario_solicitante) REFERENCES usuario(id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_agendamento_sala FOREIGN KEY (id_predio, numero_sala) REFERENCES sala(id_predio, numero_sala) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_agendamento_tipo FOREIGN KEY (id_tipo) REFERENCES tipo_agendamento(id_tipo) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT ck_agendamento_datas CHECK (data_hora_inicio < data_hora_fim)
);

-- RN12: Toda mudança de situação de um agendamento deve gerar um novo registro de histórico.
CREATE TABLE historico_status_agendamento (
    id_agendamento INT NOT NULL,
    numero_sequencia INT NOT NULL,
    situacao_anterior ENUM('SOLICITADO', 'APROVADO', 'CONFIRMADO', 'REALIZADO', 'CANCELADO') NULL,
    situacao_nova ENUM('SOLICITADO', 'APROVADO', 'CONFIRMADO', 'REALIZADO', 'CANCELADO') NOT NULL,
    data_hora_mudanca DATETIME NOT NULL,
    observacao VARCHAR(250) NULL,
    id_usuario_responsavel INT NOT NULL,
    CONSTRAINT pk_historico PRIMARY KEY (id_agendamento, numero_sequencia),
    CONSTRAINT fk_historico_agendamento FOREIGN KEY (id_agendamento) REFERENCES agendamento(id_agendamento) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_historico_usuario FOREIGN KEY (id_usuario_responsavel) REFERENCES usuario(id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- RN13: A quantidade de um recurso reservado em um agendamento não pode exceder a quantidade total disponível.
-- RN18: Todo recurso reservado em um agendamento deve pertencer ao catálogo de recursos.
CREATE TABLE agendamento_recurso (
    id_agendamento INT NOT NULL,
    id_recurso INT NOT NULL,
    quantidade_reservada INT NOT NULL,
    situacao_devolucao ENUM('PENDENTE', 'DEVOLVIDO', 'AVARIADO') NULL,
    CONSTRAINT pk_agendamento_recurso PRIMARY KEY (id_agendamento, id_recurso),
    CONSTRAINT fk_ar_agendamento FOREIGN KEY (id_agendamento) REFERENCES agendamento(id_agendamento) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_ar_recurso FOREIGN KEY (id_recurso) REFERENCES recurso(id_recurso) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT ck_ar_qtd CHECK (quantidade_reservada > 0)
);

-- RN14: Um agendamento pode ter participantes além do solicitante, cada um com um papel e status de confirmação.
CREATE TABLE participacao_agendamento (
    id_agendamento INT NOT NULL,
    id_usuario INT NOT NULL,
    papel_participante ENUM('CONVIDADO', 'RESPONSAVEL_TECNICO', 'OBSERVADOR') NOT NULL,
    status_confirmacao ENUM('PENDENTE', 'CONFIRMADO', 'RECUSADO') NOT NULL,
    data_confirmacao DATETIME NULL,
    CONSTRAINT pk_participacao PRIMARY KEY (id_agendamento, id_usuario),
    CONSTRAINT fk_part_agendamento FOREIGN KEY (id_agendamento) REFERENCES agendamento(id_agendamento) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_part_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE INDEX idx_agendamento_data_inicio ON agendamento (data_hora_inicio);
CREATE INDEX idx_agendamento_situacao ON agendamento (situacao_atual);
CREATE INDEX idx_historico_situacao_nova ON historico_status_agendamento (situacao_nova);