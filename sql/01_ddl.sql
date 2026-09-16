-- ==============================================================================
-- AgendaUCB — Script físico (DDL)
-- Executável do início ao fim em base limpa.
-- As regras de negócio (RN01 a RN21) referenciadas nos comentários abaixo
-- correspondem à numeração do Documento de Escopo e Regras de Negócio (A1).
-- ==============================================================================

DROP DATABASE IF EXISTS agendaucb;
CREATE DATABASE agendaucb;
USE agendaucb;

-- RN06: A sigla de cada departamento deve ser única no sistema.
-- RN07: Um departamento pode estar subordinado a outro (autorrelacionamento hierárquico).
CREATE TABLE departamento (
    id_departamento INT AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    sigla VARCHAR(10) NOT NULL,
    data_criacao DATE NOT NULL,
    id_departamento_pai INT NULL,
    CONSTRAINT pk_departamento PRIMARY KEY (id_departamento),
    CONSTRAINT uq_departamento_sigla UNIQUE (sigla), -- RN06
    CONSTRAINT fk_departamento_pai FOREIGN KEY (id_departamento_pai) REFERENCES departamento(id_departamento) ON DELETE RESTRICT ON UPDATE CASCADE -- RN07
);

-- RN01: O CPF de cada usuário deve ser único.
-- RN02: O e-mail institucional de cada usuário deve ser único.
-- RN03: A matrícula de cada aluno deve ser única.
-- RN04: A matrícula docente de cada professor deve ser única.
-- RN05: Todo usuário pertence a exatamente um departamento.
-- RN08: O tipo de usuário (ALUNO, PROFESSOR, ADMINISTRATIVO) define quais atributos
--       de especialização são preenchidos (generalização/especialização em tabela única).
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
    matricula VARCHAR(15) NULL,
    curso VARCHAR(80) NULL,
    semestre_atual INT NULL,
    situacao_academica ENUM('ATIVO', 'TRANCADO', 'FORMADO') NULL,
    matricula_docente VARCHAR(15) NULL,
    titulacao VARCHAR(40) NULL,
    regime_trabalho ENUM('Dedicação Exclusiva', '40 horas', '20 horas') NULL,
    cargo VARCHAR(60) NULL,
    setor VARCHAR(60) NULL,
    ramal VARCHAR(6) NULL,
    CONSTRAINT pk_usuario PRIMARY KEY (id_usuario),
    CONSTRAINT uq_usuario_cpf UNIQUE (cpf), -- RN01
    CONSTRAINT uq_usuario_email UNIQUE (email_institucional), -- RN02
    CONSTRAINT uq_usuario_matricula UNIQUE (matricula), -- RN03
    CONSTRAINT uq_usuario_mat_doc UNIQUE (matricula_docente), -- RN04
    CONSTRAINT fk_usuario_depto FOREIGN KEY (id_departamento) REFERENCES departamento(id_departamento) ON DELETE RESTRICT ON UPDATE CASCADE -- RN05
);

-- RN19: A data de nascimento de um usuário não pode ser futura.
-- Implementada via TRIGGER porque o MySQL não aceita CURRENT_DATE em CHECK CONSTRAINT
-- de forma confiável em todas as versões do SGBD.
DELIMITER //
CREATE TRIGGER trg_ck_usuario_data_nasc_ins
BEFORE INSERT ON usuario
FOR EACH ROW
BEGIN
    IF NEW.data_nascimento > CURRENT_DATE THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Erro de Restrição (RN19): A data de nascimento não pode ser futura.';
    END IF;
END; //

CREATE TRIGGER trg_ck_usuario_data_nasc_upd
BEFORE UPDATE ON usuario
FOR EACH ROW
BEGIN
    IF NEW.data_nascimento > CURRENT_DATE THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Erro de Restrição (RN19): A data de nascimento não pode ser futura.';
    END IF;
END; //
DELIMITER ;

-- RN09: Um usuário pode ter múltiplos telefones de contato (entidade fraca,
--       identificada pela dependência de id_usuario).
CREATE TABLE telefone_usuario (
    id_usuario INT NOT NULL,
    numero_telefone VARCHAR(20) NOT NULL,
    tipo_telefone ENUM('CELULAR', 'FIXO', 'RAMAL') NULL,
    CONSTRAINT pk_telefone PRIMARY KEY (id_usuario, numero_telefone),
    CONSTRAINT fk_telefone_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE ON UPDATE CASCADE -- RN09
);

-- RN10: Todo prédio deve ter ao menos 1 andar cadastrado.
CREATE TABLE predio (
    id_predio INT AUTO_INCREMENT,
    nome VARCHAR(60) NOT NULL,
    logradouro VARCHAR(120) NOT NULL,
    cep VARCHAR(8) NULL,
    quantidade_andares INT NOT NULL,
    CONSTRAINT pk_predio PRIMARY KEY (id_predio),
    CONSTRAINT ck_predio_andares CHECK (quantidade_andares > 0) -- RN10
);

-- RN11: Uma sala pertence a exatamente um prédio e seu número é único dentro dele.
-- RN12: A capacidade de uma sala deve ser um valor positivo.
CREATE TABLE sala (
    numero_sala VARCHAR(10) NOT NULL,
    id_predio INT NOT NULL,
    capacidade INT NOT NULL,
    tipo_sala ENUM('SALA_DE_AULA', 'LABORATORIO', 'AUDITORIO', 'REUNIAO') NOT NULL,
    possui_projetor BOOLEAN NOT NULL,
    CONSTRAINT pk_sala PRIMARY KEY (id_predio, numero_sala), -- RN11
    CONSTRAINT fk_sala_predio FOREIGN KEY (id_predio) REFERENCES predio(id_predio) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT ck_sala_capacidade CHECK (capacidade > 0) -- RN12
);

-- RN13: Cada tipo de agendamento define se exige aprovação prévia da coordenação.
CREATE TABLE tipo_agendamento (
    id_tipo INT AUTO_INCREMENT,
    descricao VARCHAR(80) NOT NULL,
    duracao_padrao_minutos INT NULL,
    requer_aprovacao BOOLEAN NOT NULL, -- RN13
    CONSTRAINT pk_tipo_agendamento PRIMARY KEY (id_tipo)
);

-- RN14: A quantidade total disponível de um recurso não pode ser negativa.
CREATE TABLE recurso (
    id_recurso INT AUTO_INCREMENT,
    nome VARCHAR(80) NOT NULL,
    tipo_recurso ENUM('EQUIPAMENTO', 'CONSUMO', 'CHAVE') NOT NULL,
    patrimonio VARCHAR(30) NULL,
    quantidade_total_disponivel INT NOT NULL,
    CONSTRAINT pk_recurso PRIMARY KEY (id_recurso),
    CONSTRAINT ck_recurso_qtd CHECK (quantidade_total_disponivel >= 0) -- RN14
);

-- RN15: Todo agendamento é feito por um usuário cadastrado, para uma sala específica
--       de um prédio específico, e classificado em um tipo de agendamento.
-- RN16: O horário de término de um agendamento deve ser posterior ao horário de início.
-- RN17: Uma sala não pode ter dois agendamentos com sobreposição de horário (verificado
--       pela aplicação/consulta, pois o SGBD não impõe exclusão de intervalos nativamente).
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
    CONSTRAINT fk_agendamento_usuario FOREIGN KEY (id_usuario_solicitante) REFERENCES usuario(id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE, -- RN15
    CONSTRAINT fk_agendamento_sala FOREIGN KEY (id_predio, numero_sala) REFERENCES sala(id_predio, numero_sala) ON DELETE RESTRICT ON UPDATE CASCADE, -- RN15
    CONSTRAINT fk_agendamento_tipo FOREIGN KEY (id_tipo) REFERENCES tipo_agendamento(id_tipo) ON DELETE RESTRICT ON UPDATE CASCADE, -- RN15
    CONSTRAINT ck_agendamento_datas CHECK (data_hora_inicio < data_hora_fim) -- RN16
);

-- RN18: Toda mudança de situação de um agendamento deve ficar registrada em um
--       histórico sequencial (atributo temporal — situação ao longo do tempo).
--       Entidade fraca, identificada pela dependência de id_agendamento.
CREATE TABLE historico_status_agendamento (
    id_agendamento INT NOT NULL,
    numero_sequencia INT NOT NULL,
    situacao_anterior ENUM('SOLICITADO', 'APROVADO', 'CONFIRMADO', 'REALIZADO', 'CANCELADO') NULL,
    situacao_nova ENUM('SOLICITADO', 'APROVADO', 'CONFIRMADO', 'REALIZADO', 'CANCELADO') NOT NULL,
    data_hora_mudanca DATETIME NOT NULL,
    observacao VARCHAR(250) NULL,
    id_usuario_responsavel INT NOT NULL,
    CONSTRAINT pk_historico PRIMARY KEY (id_agendamento, numero_sequencia), -- RN18
    CONSTRAINT fk_historico_agendamento FOREIGN KEY (id_agendamento) REFERENCES agendamento(id_agendamento) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_historico_usuario FOREIGN KEY (id_usuario_responsavel) REFERENCES usuario(id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- RN20: Um agendamento pode reservar múltiplos recursos, e cada reserva registra a
--       quantidade retirada e a situação de devolução (relacionamento N:N com atributo próprio).
CREATE TABLE agendamento_recurso (
    id_agendamento INT NOT NULL,
    id_recurso INT NOT NULL,
    quantidade_reservada INT NOT NULL,
    situacao_devolucao ENUM('PENDENTE', 'DEVOLVIDO', 'AVARIADO') NULL,
    CONSTRAINT pk_agendamento_recurso PRIMARY KEY (id_agendamento, id_recurso),
    CONSTRAINT fk_ar_agendamento FOREIGN KEY (id_agendamento) REFERENCES agendamento(id_agendamento) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_ar_recurso FOREIGN KEY (id_recurso) REFERENCES recurso(id_recurso) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT ck_ar_qtd CHECK (quantidade_reservada > 0) -- RN20
);

-- RN21: Um usuário pode participar de um agendamento com um único papel por vez
--       (relacionamento N:N com atributo próprio entre agendamento e usuário).
CREATE TABLE participacao_agendamento (
    id_agendamento INT NOT NULL,
    id_usuario INT NOT NULL,
    papel_participante ENUM('CONVIDADO', 'RESPONSAVEL_TECNICO', 'OBSERVADOR') NOT NULL,
    status_confirmacao ENUM('PENDENTE', 'CONFIRMADO', 'RECUSADO') NOT NULL,
    data_confirmacao DATETIME NULL,
    CONSTRAINT pk_participacao PRIMARY KEY (id_agendamento, id_usuario), -- RN21
    CONSTRAINT fk_part_agendamento FOREIGN KEY (id_agendamento) REFERENCES agendamento(id_agendamento) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_part_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- ==============================================================================
-- Índices de apoio (prefixo idx_), criados sobre colunas usadas com frequência
-- em filtros e junções nas consultas de verificação (03_consultas.sql).
-- ==============================================================================

-- Acelera filtros e faixas de data sobre agendamentos (ex.: Q3, Q13).
CREATE INDEX idx_agendamento_data_inicio ON agendamento (data_hora_inicio);

-- Acelera filtros por situação do agendamento (ex.: Q13, Q14, Q15).
CREATE INDEX idx_agendamento_situacao ON agendamento (situacao_atual);

-- Acelera filtros por tipo e situação acadêmica de usuário (ex.: Q1, Q4, Q5).
CREATE INDEX idx_usuario_tipo_situacao ON usuario (tipo_usuario, situacao_academica);

-- Acelera a junção do histórico com o status buscado (ex.: Q14).
CREATE INDEX idx_historico_situacao_nova ON historico_status_agendamento (situacao_nova);
