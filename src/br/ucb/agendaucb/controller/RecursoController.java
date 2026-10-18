package br.ucb.agendaucb.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import br.ucb.agendaucb.dao.RecursoDAO;
import br.ucb.agendaucb.model.Recurso;
import br.ucb.agendaucb.model.enums.TipoRecurso;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecursoController implements HttpHandler {

    private final RecursoDAO recursoDAO = new RecursoDAO();

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

        if (id != null) {
            Recurso r = recursoDAO.buscarPorId(id);
            if (r != null) {
                enviarResposta(exchange, 200, recursoParaJson(r));
            } else {
                enviarResposta(exchange, 404, "{\"erro\": \"Recurso não encontrado\"}");
            }
        } else {
            List<Recurso> lista = recursoDAO.listarTudo();
            enviarResposta(exchange, 200, listaParaJson(lista));
        }
    }

    private void tratarPost(HttpExchange exchange) throws SQLException, IOException {
        String corpoBody = lerCorpoRequisicao(exchange);

        String nome = extrairValorJson(corpoBody, "nome");
        String tipoStr = extrairValorJson(corpoBody, "tipo_recurso");
        String patrimonio = extrairValorJson(corpoBody, "patrimonio");
        String qtdStr = extrairValorJson(corpoBody, "quantidade_total_disponivel");

        if (nome == null || tipoStr == null || patrimonio == null || qtdStr == null) {
            enviarResposta(exchange, 400, "{\"erro\": \"Campos obrigatórios ausentes no JSON\"}");
            return;
        }

        Recurso r = new Recurso(
                0,
                nome,
                TipoRecurso.valueOf(tipoStr.toUpperCase()),
                patrimonio,
                Integer.parseInt(qtdStr)
        );

        recursoDAO.cadastrar(r);
        enviarResposta(exchange, 201, "{\"mensagem\": \"Recurso cadastrado com sucesso!\"}");
    }

    private void tratarPut(HttpExchange exchange) throws SQLException, IOException {
        String corpoBody = lerCorpoRequisicao(exchange);

        
        String idStr = extrairValorJson(corpoBody, "id_recurso");
        if (idStr == null) {
            String query = exchange.getRequestURI().getQuery();
            Integer idQuery = obterParametroInt(query, "id");
            if (idQuery != null) {
                idStr = String.valueOf(idQuery);
            }
        }

        if (idStr == null) {
            enviarResposta(exchange, 400, "{\"erro\": \"ID do recurso é obrigatório para atualização\"}");
            return;
        }

        int idRecurso = Integer.parseInt(idStr);

        
        Recurso recursoExistente = recursoDAO.buscarPorId(idRecurso);
        if (recursoExistente == null) {
            enviarResposta(exchange, 404, "{\"erro\": \"Recurso não encontrado para atualização\"}");
            return;
        }

       
        String nome = extrairValorJson(corpoBody, "nome");
        String tipoStr = extrairValorJson(corpoBody, "tipo_recurso");
        String patrimonio = extrairValorJson(corpoBody, "patrimonio");
        String qtdStr = extrairValorJson(corpoBody, "quantidade_total_disponivel");

        
        if (nome != null) {
            recursoExistente.setNome(nome);
        }
        if (tipoStr != null) {
            recursoExistente.setTipo_recurso(TipoRecurso.valueOf(tipoStr.toUpperCase()));
        }
        if (patrimonio != null) {
            recursoExistente.setPatrimonio(patrimonio);
        }
        if (qtdStr != null) {
            recursoExistente.setQntd_disponivel(Integer.parseInt(qtdStr));
        }

        
        recursoDAO.atualizar(recursoExistente);
        enviarResposta(exchange, 200, "{\"mensagem\": \"Recurso atualizado com sucesso!\"}");
    }

    private void tratarDelete(HttpExchange exchange) throws SQLException, IOException {
        String query = exchange.getRequestURI().getQuery();
        Integer id = obterParametroInt(query, "id");

        if (id == null) {
            String corpoBody = lerCorpoRequisicao(exchange);
            String idStr = extrairValorJson(corpoBody, "id_recurso");
            if (idStr != null) {
                id = Integer.parseInt(idStr);
            }
        }

        if (id != null) {
            Recurso r = recursoDAO.buscarPorId(id);
            if (r == null) {
                enviarResposta(exchange, 404, "{\"erro\": \"Recurso não encontrado para exclusão\"}");
                return;
            }
            recursoDAO.deletar(id);
            enviarResposta(exchange, 200, "{\"mensagem\": \"Recurso removido com sucesso!\"}");
        } else {
            enviarResposta(exchange, 400, "{\"erro\": \"ID do recurso não informado\"}");
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
        if (json == null || json.isEmpty()) return null;
        Pattern pattern = Pattern.compile("\"" + chave + "\"\\s*:\\s*\"?([^\",}]+)\"?");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1).trim().replace("\"", "");
        }
        return null;
    }

    private Integer obterParametroInt(String query, String nomeParam) {
        if (query == null || query.isEmpty()) return null;
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

    private String recursoParaJson(Recurso r) {
        return String.format(
                "{\"id_recurso\": %d, \"nome\": \"%s\", \"tipo_recurso\": \"%s\", \"patrimonio\": \"%s\", \"quantidade_total_disponivel\": %d}",
                r.getId_recurso(),
                r.getNome(),
                r.getTipo_recurso().name(),
                r.getPatrimonio(),
                r.getQntd_disponivel());
    }

    private String listaParaJson(List<Recurso> lista) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lista.size(); i++) {
            sb.append(recursoParaJson(lista.get(i)));
            if (i < lista.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}