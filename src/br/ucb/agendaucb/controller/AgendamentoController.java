package br.ucb.agendaucb.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import br.ucb.agendaucb.dao.AgendamentoDAO;
import br.ucb.agendaucb.model.Agendamento;
import br.ucb.agendaucb.model.enums.SituacaoAgendamento;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AgendamentoController implements HttpHandler {

    private final AgendamentoDAO agendamentoDAO = new AgendamentoDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        adicionarHeadersCors(exchange);

        String metodo = exchange.getRequestMethod();

        if ("OPTIONS".equalsIgnoreCase(metodo)) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        try {
            switch (metodo.toUpperCase()) {
                case "GET":
                    tratarGet(exchange);
                    break;
                case "POST":
                    tratarPost(exchange);
                    break;
                case "PUT":
                    tratarPut(exchange);
                    break;
                case "DELETE":
                    tratarDelete(exchange);
                    break;
                default:
                    enviarResposta(exchange, 405, "{\"erro\": \"Método não permitido\"}");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            enviarResposta(exchange, 500, "{\"erro\": \"Erro de banco de dados: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            e.printStackTrace();
            enviarResposta(exchange, 400, "{\"erro\": \"Requisição inválida: " + e.getMessage() + "\"}");
        }
    }

    private void tratarGet(HttpExchange exchange) throws SQLException, IOException {
        String query = exchange.getRequestURI().getQuery();
        Integer id = obterParametroInt(query, "id");
        if (id == null) {
            id = obterParametroInt(query, "id_agendamento");
        }

        if (id != null) {
            Agendamento a = agendamentoDAO.buscarPorId(id);
            if (a != null) {
                enviarResposta(exchange, 200, agendamentoParaJson(a));
            } else {
                enviarResposta(exchange, 404, "{\"erro\": \"Agendamento não encontrado\"}");
            }
        } else {
            List<Agendamento> lista = agendamentoDAO.listarTudo();
            enviarResposta(exchange, 200, listaParaJson(lista));
        }
    }

    private void tratarPost(HttpExchange exchange) throws SQLException, IOException {
        String corpoBody = lerCorpoRequisicao(exchange);

        String inicioStr = extrairValorJson(corpoBody, "inicio");
        if (inicioStr == null) {
            inicioStr = extrairValorJson(corpoBody, "data_hora_inicio");
        }

        String fimStr = extrairValorJson(corpoBody, "fim");
        if (fimStr == null) {
            fimStr = extrairValorJson(corpoBody, "data_hora_fim");
        }

        String situacaoStr = extrairValorJson(corpoBody, "situacao");
        if (situacaoStr == null) {
            situacaoStr = extrairValorJson(corpoBody, "situacao_atual");
        }

        String motivo = extrairValorJson(corpoBody, "motivo");
        if (motivo == null) {
            motivo = extrairValorJson(corpoBody, "motivo_descricao");
        }

        String idUsuarioStr = extrairValorJson(corpoBody, "id_usuario");
        if (idUsuarioStr == null) {
            idUsuarioStr = extrairValorJson(corpoBody, "id_usuario_solicitante");
        }

        String idPredioStr = extrairValorJson(corpoBody, "id_predio");
        String numeroSala = extrairValorJson(corpoBody, "numero_sala");
        String idTipoStr = extrairValorJson(corpoBody, "id_tipo");

        if (inicioStr == null || fimStr == null || idUsuarioStr == null || idPredioStr == null || numeroSala == null
                || idTipoStr == null) {
            enviarResposta(exchange, 400, "{\"erro\": \"Campos obrigatórios ausentes no JSON\"}");
            return;
        }

        Timestamp inicio = parseTimestamp(inicioStr);
        Timestamp fim = parseTimestamp(fimStr);
        if (inicio == null || fim == null) {
            enviarResposta(exchange, 400,
                    "{\"erro\": \"Formato de data/hora inválido. Use AAAA-MM-DD HH:mm:ss ou AAAA-MM-DDTHH:mm:ss\"}");
            return;
        }

        if (!fim.after(inicio)) {
            enviarResposta(exchange, 400,
                    "{\"erro\": \"A data/hora de fim deve ser posterior à data/hora de início\"}");
            return;
        }

        int idUsuario, idPredio, idTipo;
        try {
            idUsuario = Integer.parseInt(idUsuarioStr);
            idPredio = Integer.parseInt(idPredioStr);
            idTipo = Integer.parseInt(idTipoStr);
        } catch (NumberFormatException e) {
            enviarResposta(exchange, 400, "{\"erro\": \"IDs de usuário, prédio e tipo devem ser números inteiros\"}");
            return;
        }

        SituacaoAgendamento situacaoEnum = SituacaoAgendamento.SOLICITADO;
        if (situacaoStr != null) {
            try {
                situacaoEnum = SituacaoAgendamento.valueOf(situacaoStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                enviarResposta(exchange, 400, "{\"erro\": \"Situação de agendamento inválida\"}");
                return;
            }
        }

        Agendamento a = new Agendamento(
                0,
                inicio,
                fim,
                situacaoEnum,
                motivo != null ? motivo : "",
                new Timestamp(System.currentTimeMillis()),
                idUsuario,
                idPredio,
                numeroSala,
                idTipo);

        boolean cadastrado = agendamentoDAO.cadastrar(a);
        if (cadastrado) {
            enviarResposta(exchange, 201, "{\"mensagem\": \"Agendamento cadastrado com sucesso!\"}");
        } else {
            enviarResposta(exchange, 409,
                    "{\"erro\": \"Conflito de horário: a sala já possui um agendamento no período informado\"}");
        }
    }

    private void tratarPut(HttpExchange exchange) throws SQLException, IOException {
        String corpoBody = lerCorpoRequisicao(exchange);

        String idStr = extrairValorJson(corpoBody, "id_agendamento");
        if (idStr == null) {
            idStr = extrairValorJson(corpoBody, "id");
        }
        if (idStr == null) {
            String query = exchange.getRequestURI().getQuery();
            Integer idQuery = obterParametroInt(query, "id");
            if (idQuery != null) {
                idStr = String.valueOf(idQuery);
            }
        }

        if (idStr == null) {
            enviarResposta(exchange, 400, "{\"erro\": \"ID do agendamento é obrigatório para atualização\"}");
            return;
        }

        int idAgendamento;
        try {
            idAgendamento = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            enviarResposta(exchange, 400, "{\"erro\": \"ID do agendamento deve ser um número inteiro\"}");
            return;
        }

        Agendamento agendamentoExistente = agendamentoDAO.buscarPorId(idAgendamento);
        if (agendamentoExistente == null) {
            enviarResposta(exchange, 404, "{\"erro\": \"Agendamento não encontrado para atualização\"}");
            return;
        }

        String inicioStr = extrairValorJson(corpoBody, "inicio");
        if (inicioStr == null) {
            inicioStr = extrairValorJson(corpoBody, "data_hora_inicio");
        }

        String fimStr = extrairValorJson(corpoBody, "fim");
        if (fimStr == null) {
            fimStr = extrairValorJson(corpoBody, "data_hora_fim");
        }

        String situacaoStr = extrairValorJson(corpoBody, "situacao");
        if (situacaoStr == null) {
            situacaoStr = extrairValorJson(corpoBody, "situacao_atual");
        }

        String motivo = extrairValorJson(corpoBody, "motivo");
        if (motivo == null) {
            motivo = extrairValorJson(corpoBody, "motivo_descricao");
        }

        String idUsuarioStr = extrairValorJson(corpoBody, "id_usuario");
        if (idUsuarioStr == null) {
            idUsuarioStr = extrairValorJson(corpoBody, "id_usuario_solicitante");
        }

        String idPredioStr = extrairValorJson(corpoBody, "id_predio");
        String numeroSala = extrairValorJson(corpoBody, "numero_sala");
        String idTipoStr = extrairValorJson(corpoBody, "id_tipo");

        if (inicioStr != null) {
            Timestamp inicio = parseTimestamp(inicioStr);
            if (inicio != null)
                agendamentoExistente.setInicio(inicio);
        }
        if (fimStr != null) {
            Timestamp fim = parseTimestamp(fimStr);
            if (fim != null)
                agendamentoExistente.setFim(fim);
        }
        if (situacaoStr != null) {
            try {
                agendamentoExistente.setSituacao(SituacaoAgendamento.valueOf(situacaoStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                enviarResposta(exchange, 400, "{\"erro\": \"Situação de agendamento inválida\"}");
                return;
            }
        }
        if (motivo != null)
            agendamentoExistente.setMotivo(motivo);
        if (idUsuarioStr != null) {
            try {
                agendamentoExistente.setIdUsuario(Integer.parseInt(idUsuarioStr));
            } catch (NumberFormatException ignored) {
            }
        }
        if (idPredioStr != null) {
            try {
                agendamentoExistente.setIdPredio(Integer.parseInt(idPredioStr));
            } catch (NumberFormatException ignored) {
            }
        }
        if (numeroSala != null)
            agendamentoExistente.setNumeroSala(numeroSala);
        if (idTipoStr != null) {
            try {
                agendamentoExistente.setIdTipo(Integer.parseInt(idTipoStr));
            } catch (NumberFormatException ignored) {
            }
        }

        agendamentoDAO.atualizar(agendamentoExistente);
        enviarResposta(exchange, 200, "{\"mensagem\": \"Agendamento atualizado com sucesso!\"}");
    }

    private void tratarDelete(HttpExchange exchange) throws SQLException, IOException {
        String query = exchange.getRequestURI().getQuery();
        Integer id = obterParametroInt(query, "id");
        if (id == null) {
            id = obterParametroInt(query, "id_agendamento");
        }

        if (id == null) {
            String corpoBody = lerCorpoRequisicao(exchange);
            String idStr = extrairValorJson(corpoBody, "id_agendamento");
            if (idStr == null) {
                idStr = extrairValorJson(corpoBody, "id");
            }
            if (idStr != null) {
                try {
                    id = Integer.parseInt(idStr);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        if (id != null) {
            Agendamento a = agendamentoDAO.buscarPorId(id);
            if (a == null) {
                enviarResposta(exchange, 404, "{\"erro\": \"Agendamento não encontrado para exclusão\"}");
                return;
            }
            agendamentoDAO.deletar(id);
            enviarResposta(exchange, 200, "{\"mensagem\": \"Agendamento removido com sucesso!\"}");
        } else {
            enviarResposta(exchange, 400, "{\"erro\": \"ID do agendamento não informado\"}");
        }
    }

    private Timestamp parseTimestamp(String valor) {
        if (valor == null || valor.trim().isEmpty())
            return null;
        try {
            String formatado = valor.trim().replace("T", " ");
            if (formatado.length() == 10) {
                formatado += " 00:00:00";
            } else if (formatado.length() == 16) {
                formatado += ":00";
            }
            return Timestamp.valueOf(formatado);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void enviarResposta(HttpExchange exchange, int statusCode, String respostaJson) throws IOException {
        byte[] bytes = respostaJson.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void adicionarHeadersCors(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private String lerCorpoRequisicao(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String extrairValorJson(String json, String chave) {
        if (json == null || json.isEmpty())
            return null;
        Pattern pattern = Pattern.compile("\"" + chave + "\"\\s*:\\s*\"?([^\",}]+)\"?");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1).trim().replace("\"", "");
        }
        return null;
    }

    private Integer obterParametroInt(String query, String nomeParam) {
        if (query == null || query.isEmpty())
            return null;
        for (String param : query.split("&")) {
            String[] par = param.split("=");
            if (par.length == 2 && par[0].equalsIgnoreCase(nomeParam)) {
                try {
                    return Integer.parseInt(par[1]);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }

    private String agendamentoParaJson(Agendamento a) {
        return String.format(
                "{\"id_agendamento\": %d, \"data_hora_inicio\": \"%s\", \"data_hora_fim\": \"%s\", \"situacao_atual\": \"%s\", \"motivo_descricao\": \"%s\", \"data_criacao\": \"%s\", \"id_usuario_solicitante\": %d, \"id_predio\": %d, \"numero_sala\": \"%s\", \"id_tipo\": %d}",
                a.getIdAgendamento(),
                a.getInicio() != null ? a.getInicio().toString() : "",
                a.getFim() != null ? a.getFim().toString() : "",
                a.getSituacao() != null ? a.getSituacao().name() : "",
                a.getMotivo() != null ? a.getMotivo() : "",
                a.getDataCriacao() != null ? a.getDataCriacao().toString() : "",
                a.getIdUsuario(),
                a.getIdPredio(),
                a.getNumeroSala() != null ? a.getNumeroSala() : "",
                a.getIdTipo());
    }

    private String listaParaJson(List<Agendamento> lista) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lista.size(); i++) {
            sb.append(agendamentoParaJson(lista.get(i)));
            if (i < lista.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}