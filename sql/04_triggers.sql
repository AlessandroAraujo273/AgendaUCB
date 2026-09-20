-- ============================================================================
-- PROJETO: AGENDA UCB - GESTÃO DE EVENTOS E ESPAÇOS ACADÊMICOS
-- ARQUIVO: sql/04_triggers.sql
-- AUTOR: Carlos André Serpa Nataniel
-- OBJETIVO: Automação das Regras de Negócio RN06, RN09 e RN10
-- ============================================================================

USE agenda_ucb;

DELIMITER $$

-- Trigger 1: Validação preventiva de horários da reserva (RN06)
DROP TRIGGER IF EXISTS trg_valida_horario_reserva $$
CREATE TRIGGER trg_valida_horario_reserva
BEFORE INSERT ON reserva
FOR EACH ROW
BEGIN
    IF NEW.hora_fim <= NEW.hora_inicio THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Erro de Negócio (RN06): O horário de término deve ser estritamente posterior ao início.';
    END IF;
END $$

-- Trigger 2: Auditoria automática do histórico de transição de status (RN09, RN10)
DROP TRIGGER IF EXISTS trg_historico_reserva_update $$
CREATE TRIGGER trg_historico_reserva_update
AFTER UPDATE ON reserva
FOR EACH ROW
BEGIN
    IF OLD.status <> NEW.status THEN
        INSERT INTO historico_status_reserva (
            id_reserva,
            status_anterior,
            status_novo,
            data_hora_mudanca,
            justificativa
        ) VALUES (
            NEW.id_reserva,
            OLD.status,
            NEW.status,
            NOW(),
            CONCAT('Transição automática de status registrada via trigger.')
        );
    END IF;
END $$

DELIMITER ;