package br.ucb.agendaucb.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import br.ucb.agendaucb.dao.SalaDAO;
import br.ucb.agendaucb.model.Sala;
import br.ucb.agendaucb.model.enums.TipoSala;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SalaController implements HttpHandler {

    private final SalaDAO salaDAO = new SalaDAO();

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
        Integer idPredio = obterParametroInt(query, "id_predio");
        if (idPredio == null) {
            idPredio = obterParametroInt(query, "idPredio");
        }

        String numeroSala = obterParametroString(query, "numero_sala");
        if (numeroSala == null) {
            numeroSala = obterParametroString(query, "numeroSala");
        }

        if (idPredio != null && numeroSala != null) {
            Sala s = salaDAO.buscarPorChave(idPredio, numeroSala);
            if (s != null) {
                enviarResposta(exchange, 200, salaParaJson(s));
            } else {
                enviarResposta(exchange, 404, "{\"erro\": \"Sala não encontrada\"}");
            }
        } else if (idPredio != null) {
            List<Sala> lista = salaDAO.listarPorPredio(idPredio);
            enviarResposta(exchange, 200, listaParaJson(lista));
        } else {
            List<Sala> lista = salaDAO.listarTudo();
            enviarResposta(exchange, 200, listaParaJson(lista));
        }
    }

    private void tratarPost(HttpExchange exchange) throws SQLException, IOException {
        String corpoBody = lerCorpoRequisicao(exchange);

        String idPredioStr = extrairValorJson(corpoBody, "id_predio");
        String numeroSala = extrairValorJson(corpoBody, "numero_sala");
        String capacidadeStr = extrairValorJson(corpoBody, "capacidade");
        String tipoStr = extrairValorJson(corpoBody, "tipo_sala");
        String projetorStr = extrairValorJson(corpoBody, "possui_projetor");

        if (idPredioStr == null || numeroSala == null || capacidadeStr == null || tipoStr == null
                || projetorStr == null) {
            enviarResposta(exchange, 400, "{\"erro\": \"Campos obrigatórios ausentes no JSON\"}");
            return;
        }

        int idPredio;
        int capacidade;
        try {
            idPredio = Integer.parseInt(idPredioStr);
            capacidade = Integer.parseInt(capacidadeStr);
        } catch (NumberFormatException e) {
            enviarResposta(exchange, 400, "{\"erro\": \"ID do prédio e capacidade devem ser números inteiros\"}");
            return;
        }

        TipoSala tipoEnum;
        try {
            tipoEnum = TipoSala.valueOf(tipoStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            enviarResposta(exchange, 400, "{\"erro\": \"Tipo de sala inválido\"}");
            return;
        }

        boolean possuiProjetor = Boolean.parseBoolean(projetorStr);

        Sala s = new Sala(idPredio, numeroSala, capacidade, tipoEnum, possuiProjetor);
        salaDAO.cadastrar(s);
        enviarResposta(exchange, 201, "{\"mensagem\": \"Sala cadastrada com sucesso!\"}");
    }

    private void tratarPut(HttpExchange exchange) throws SQLException, IOException {
        String corpoBody = lerCorpoRequisicao(exchange);

        String idPredioStr = extrairValorJson(corpoBody, "id_predio");
        String numeroSala = extrairValorJson(corpoBody, "numero_sala");

        if (idPredioStr == null || numeroSala == null) {
            String query = exchange.getRequestURI().getQuery();
            if (idPredioStr == null) {
                Integer pQuery = obterParametroInt(query, "id_predio");
                if (pQuery != null)
                    idPredioStr = String.valueOf(pQuery);
            }
            if (numeroSala == null) {
                numeroSala = obterParametroString(query, "numero_sala");
            }
        }

        if (idPredioStr == null || numeroSala == null) {
            enviarResposta(exchange, 400,
                    "{\"erro\": \"ID do prédio e Número da sala são obrigatórios para atualização\"}");
            return;
        }

        int idPredio;
        try {
            idPredio = Integer.parseInt(idPredioStr);
        } catch (NumberFormatException e) {
            enviarResposta(exchange, 400, "{\"erro\": \"ID do prédio deve ser um número inteiro\"}");
            return;
        }

        Sala salaExistente = salaDAO.buscarPorChave(idPredio, numeroSala);
        if (salaExistente == null) {
            enviarResposta(exchange, 404, "{\"erro\": \"Sala não encontrada para atualização\"}");
            return;
        }

        String capacidadeStr = extrairValorJson(corpoBody, "capacidade");
        String tipoStr = extrairValorJson(corpoBody, "tipo_sala");
        String projetorStr = extrairValorJson(corpoBody, "possui_projetor");

        if (capacidadeStr != null) {
            try {
                salaExistente.setCapacidade(Integer.parseInt(capacidadeStr));
            } catch (NumberFormatException e) {
                enviarResposta(exchange, 400, "{\"erro\": \"Capacidade deve ser um número inteiro\"}");
                return;
            }
        }

        if (tipoStr != null) {
            try {
                salaExistente.setTipo_sala(TipoSala.valueOf(tipoStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                enviarResposta(exchange, 400, "{\"erro\": \"Tipo de sala inválido\"}");
                return;
            }
        }

        if (projetorStr != null) {
            salaExistente.setPossui_projetor(Boolean.parseBoolean(projetorStr));
        }

        salaDAO.atualizar(salaExistente);
        enviarResposta(exchange, 200, "{\"mensagem\": \"Sala atualizada com sucesso!\"}");
    }

    private void tratarDelete(HttpExchange exchange) throws SQLException, IOException {
        String query = exchange.getRequestURI().getQuery();
        Integer idPredio = obterParametroInt(query, "id_predio");
        String numeroSala = obterParametroString(query, "numero_sala");

        if (idPredio == null || numeroSala == null) {
            String corpoBody = lerCorpoRequisicao(exchange);
            if (idPredio == null) {
                String pStr = extrairValorJson(corpoBody, "id_predio");
                if (pStr != null) {
                    try {
                        idPredio = Integer.parseInt(pStr);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            if (numeroSala == null) {
                numeroSala = extrairValorJson(corpoBody, "numero_sala");
            }
        }

        if (idPredio != null && numeroSala != null) {
            Sala s = salaDAO.buscarPorChave(idPredio, numeroSala);
            if (s == null) {
                enviarResposta(exchange, 404, "{\"erro\": \"Sala não encontrada para exclusão\"}");
                return;
            }
            salaDAO.deletar(idPredio, numeroSala);
            enviarResposta(exchange, 200, "{\"mensagem\": \"Sala removida com sucesso!\"}");
        } else {
            enviarResposta(exchange, 400,
                    "{\"erro\": \"ID do prédio e Número da sala são obrigatórios para exclusão\"}");
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

    private String obterParametroString(String query, String nomeParam) {
        if (query == null || query.isEmpty())
            return null;
        for (String param : query.split("&")) {
            String[] par = param.split("=");
            if (par.length == 2 && par[0].equalsIgnoreCase(nomeParam)) {
                return par[1].trim();
            }
        }
        return null;
    }

    private String salaParaJson(Sala s) {
        return String.format(
                "{\"id_predio\": %d, \"numero_sala\": \"%s\", \"capacidade\": %d, \"tipo_sala\": \"%s\", \"possui_projetor\": %b}",
                s.getId_predio(),
                s.getNumero_sala(),
                s.getCapacidade(),
                s.getTipo_sala().name(),
                s.isPossui_projetor());
    }

    private String listaParaJson(List<Sala> lista) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lista.size(); i++) {
            sb.append(salaParaJson(lista.get(i)));
            if (i < lista.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}