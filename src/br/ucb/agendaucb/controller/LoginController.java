package br.ucb.agendaucb.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import br.ucb.agendaucb.dao.UsuarioDAO;
import br.ucb.agendaucb.model.Usuario;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginController implements HttpHandler {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        adicionarHeadersCors(exchange);

        String metodo = exchange.getRequestMethod();

        if ("OPTIONS".equalsIgnoreCase(metodo)) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        try {
            if ("POST".equalsIgnoreCase(metodo)) {
                tratarLogin(exchange);
            } else {
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

    private void tratarLogin(HttpExchange exchange) throws SQLException, IOException {
        String corpoBody = lerCorpoRequisicao(exchange);

        String email = extrairValorJson(corpoBody, "email_institucional");
        if (email == null) {
            email = extrairValorJson(corpoBody, "email");
        }

        String senha = extrairValorJson(corpoBody, "senha");
        if (senha == null) {
            senha = extrairValorJson(corpoBody, "senha_hash");
        }

        if (email == null || email.trim().isEmpty() || senha == null || senha.trim().isEmpty()) {
            enviarResposta(exchange, 400, "{\"erro\": \"E-mail institucional e senha são obrigatórios\"}");
            return;
        }

        String senhaHash = criptografarSenha(senha);
        Usuario usuario = usuarioDAO.autenticar(email.trim(), senhaHash);

        if (usuario != null) {
            enviarResposta(exchange, 200, usuarioParaJson(usuario));
        } else {
            enviarResposta(exchange, 401, "{\"erro\": \"E-mail ou senha inválidos\"}");
        }
    }

    private String criptografarSenha(String senha) {
        if (senha == null)
            return null;
        try {
            MessageDigest algorithm = MessageDigest.getInstance("SHA-256");
            byte[] messageDigest = algorithm.digest(senha.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao criptografar senha: " + e.getMessage());
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
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");
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

    private String usuarioParaJson(Usuario u) {
        return String.format(
                "{\"mensagem\": \"Login realizado com sucesso!\", \"usuario\": {\"id_usuario\": %d, \"nome\": \"%s\", \"sobrenome\": \"%s\", \"email_institucional\": \"%s\", \"tipo_usuario\": \"%s\", \"id_departamento\": %d}}",
                u.getId_usuario(),
                u.getNome() != null ? u.getNome() : "",
                u.getSobrenome() != null ? u.getSobrenome() : "",
                u.getEmail_institucional() != null ? u.getEmail_institucional() : "",
                u.getTipo_usuario() != null ? u.getTipo_usuario().name() : "",
                u.getId_departamento());
    }
}