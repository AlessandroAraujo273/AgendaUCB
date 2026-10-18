package br.ucb.agendaucb.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import br.ucb.agendaucb.dao.TipoAgendamentoDAO;
import br.ucb.agendaucb.model.TipoAgendamento;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TipoAgendamentoController implements HttpHandler {

    private final TipoAgendamentoDAO tipoAgendamentoDAO = new TipoAgendamentoDAO();

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
            id = obterParametroInt(query, "id_tipo");
        }

        if (id != null) {
            TipoAgendamento t = tipoAgendamentoDAO.buscarPorId(id);
            if (t != null) {
                enviarResposta(exchange, 200, tipoParaJson(t));
            } else {
                enviarResposta(exchange, 404, "{\"erro\": \"Tipo de agendamento não encontrado\"}");
            }
        } else {
            List<TipoAgendamento> lista = tipoAgendamentoDAO.listarTudo();
            enviarResposta(exchange, 200, listaParaJson(lista));
        }
    }

    private void tratarPost(HttpExchange exchange) throws SQLException, IOException {
        String corpoBody = lerCorpoRequisicao(exchange);

        String descricao = extrairValorJson(corpoBody, "descricao");
        String duracaoStr = extrairValorJson(corpoBody, "duracao_minutos");
        if (duracaoStr == null) {
            duracaoStr = extrairValorJson(corpoBody, "duracao_padrao_minutos");
        }
        String aprovacaoStr = extrairValorJson(corpoBody, "aprovacao");
        if (aprovacaoStr == null) {
            aprovacaoStr = extrairValorJson(corpoBody, "requer_aprovacao");
        }

        if (descricao == null || duracaoStr == null || aprovacaoStr == null) {
            enviarResposta(exchange, 400, "{\"erro\": \"Campos obrigatórios ausentes no JSON\"}");
            return;
        }

        int duracao;
        try {
            duracao = Integer.parseInt(duracaoStr);
        } catch (NumberFormatException e) {
            enviarResposta(exchange, 400, "{\"erro\": \"Duração em minutos deve ser um número inteiro\"}");
            return;
        }

        boolean aprovacao = Boolean.parseBoolean(aprovacaoStr);

        TipoAgendamento t = new TipoAgendamento(0, descricao, duracao, aprovacao);
        tipoAgendamentoDAO.cadastrar(t);
        enviarResposta(exchange, 201, "{\"mensagem\": \"Tipo de agendamento cadastrado com sucesso!\"}");
    }

    private void tratarPut(HttpExchange exchange) throws SQLException, IOException {
        String corpoBody = lerCorpoRequisicao(exchange);

        String idStr = extrairValorJson(corpoBody, "id_tipo");
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
            enviarResposta(exchange, 400, "{\"erro\": \"ID do tipo de agendamento é obrigatório para atualização\"}");
            return;
        }

        int idTipo;
        try {
            idTipo = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            enviarResposta(exchange, 400, "{\"erro\": \"ID do tipo de agendamento deve ser um número inteiro\"}");
            return;
        }

        TipoAgendamento tipoExistente = tipoAgendamentoDAO.buscarPorId(idTipo);
        if (tipoExistente == null) {
            enviarResposta(exchange, 404, "{\"erro\": \"Tipo de agendamento não encontrado para atualização\"}");
            return;
        }

        String descricao = extrairValorJson(corpoBody, "descricao");
        String duracaoStr = extrairValorJson(corpoBody, "duracao_minutos");
        if (duracaoStr == null) {
            duracaoStr = extrairValorJson(corpoBody, "duracao_padrao_minutos");
        }
        String aprovacaoStr = extrairValorJson(corpoBody, "aprovacao");
        if (aprovacaoStr == null) {
            aprovacaoStr = extrairValorJson(corpoBody, "requer_aprovacao");
        }

        if (descricao != null) {
            tipoExistente.setDescricao(descricao);
        }
        if (duracaoStr != null) {
            try {
                tipoExistente.setDuracao_minutos(Integer.parseInt(duracaoStr));
            } catch (NumberFormatException e) {
                enviarResposta(exchange, 400, "{\"erro\": \"Duração em minutos deve ser um número inteiro\"}");
                return;
            }
        }
        if (aprovacaoStr != null) {
            tipoExistente.setAprovacao(Boolean.parseBoolean(aprovacaoStr));
        }

        tipoAgendamentoDAO.atualizar(tipoExistente);
        enviarResposta(exchange, 200, "{\"mensagem\": \"Tipo de agendamento atualizado com sucesso!\"}");
    }

    private void tratarDelete(HttpExchange exchange) throws SQLException, IOException {
        String query = exchange.getRequestURI().getQuery();
        Integer id = obterParametroInt(query, "id");
        if (id == null) {
            id = obterParametroInt(query, "id_tipo");
        }

        if (id == null) {
            String corpoBody = lerCorpoRequisicao(exchange);
            String idStr = extrairValorJson(corpoBody, "id_tipo");
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
            TipoAgendamento t = tipoAgendamentoDAO.buscarPorId(id);
            if (t == null) {
                enviarResposta(exchange, 404, "{\"erro\": \"Tipo de agendamento não encontrado para exclusão\"}");
                return;
            }
            tipoAgendamentoDAO.deletar(id);
            enviarResposta(exchange, 200, "{\"mensagem\": \"Tipo de agendamento removido com sucesso!\"}");
        } else {
            enviarResposta(exchange, 400, "{\"erro\": \"ID do tipo de agendamento não informado\"}");
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

    private String tipoParaJson(TipoAgendamento t) {
        return String.format(
                "{\"id_tipo\": %d, \"descricao\": \"%s\", \"duracao_minutos\": %d, \"aprovacao\": %b}",
                t.getId_tipo(),
                t.getDescricao() != null ? t.getDescricao() : "",
                t.getDuracao_minutos(),
                t.isAprovacao());
    }

    private String listaParaJson(List<TipoAgendamento> lista) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lista.size(); i++) {
            sb.append(tipoParaJson(lista.get(i)));
            if (i < lista.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}