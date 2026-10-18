package br.ucb.agendaucb.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import br.ucb.agendaucb.dao.UsuarioDAO;
import br.ucb.agendaucb.model.Administrativo;
import br.ucb.agendaucb.model.Aluno;
import br.ucb.agendaucb.model.Professor;
import br.ucb.agendaucb.model.Usuario;
import br.ucb.agendaucb.model.enums.TipoUsuario;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UsuarioController implements HttpHandler {

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
        } catch (IllegalArgumentException e) {
            enviarResposta(exchange, 400, "{\"erro\": \"Dados inválidos: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            e.printStackTrace();
            enviarResposta(exchange, 400, "{\"erro\": \"Requisição inválida: " + e.getMessage() + "\"}");
        }
    }

    private void tratarGet(HttpExchange exchange) throws SQLException, IOException {
        String query = exchange.getRequestURI().getQuery();
        Integer id = obterParametroInt(query, "id");

        if (id != null) {
            Usuario u = usuarioDAO.buscarPorId(id);
            if (u != null) {
                enviarResposta(exchange, 200, usuarioParaJson(u));
            } else {
                enviarResposta(exchange, 404, "{\"erro\": \"Usuário não encontrado\"}");
            }
        } else {
            List<Usuario> lista = usuarioDAO.listarTudo();
            enviarResposta(exchange, 200, listaParaJson(lista));
        }
    }

    private void tratarPost(HttpExchange exchange) throws SQLException, IOException {
        String corpoBody = lerCorpoRequisicao(exchange);

        String cpf = extrairValorJson(corpoBody, "cpf");
        String nome = extrairValorJson(corpoBody, "nome");
        String sobrenome = extrairValorJson(corpoBody, "sobrenome");
        String email = extrairValorJson(corpoBody, "email_institucional");
        String senha = extrairValorJson(corpoBody, "senha");
        if (senha == null) {
            senha = extrairValorJson(corpoBody, "senha_hash");
        }
        String dataNascStr = extrairValorJson(corpoBody, "data_nascimento");
        String tipoStr = extrairValorJson(corpoBody, "tipo_usuario");
        String idDepStr = extrairValorJson(corpoBody, "id_departamento");

        if (cpf == null || nome == null || sobrenome == null || email == null || senha == null || dataNascStr == null || tipoStr == null || idDepStr == null) {
            enviarResposta(exchange, 400, "{\"erro\": \"Campos obrigatórios ausentes no JSON\"}");
            return;
        }

        int idDepartamento;
        try {
            idDepartamento = Integer.parseInt(idDepStr);
        } catch (NumberFormatException e) {
            enviarResposta(exchange, 400, "{\"erro\": \"ID do departamento deve ser um número inteiro\"}");
            return;
        }

        Date dataNascimento;
        try {
            dataNascimento = Date.valueOf(dataNascStr);
        } catch (IllegalArgumentException e) {
            enviarResposta(exchange, 400, "{\"erro\": \"Data de nascimento deve estar no formato AAAA-MM-DD\"}");
            return;
        }

        TipoUsuario tipoEnum;
        try {
            tipoEnum = TipoUsuario.valueOf(tipoStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            enviarResposta(exchange, 400, "{\"erro\": \"Tipo de usuário inválido\"}");
            return;
        }

        String senhaCriptografada = criptografarSenha(senha);

        Usuario u;
        if (tipoEnum == TipoUsuario.ALUNO) {
            Aluno a = new Aluno();
            a.setMatricula(extrairValorJson(corpoBody, "matricula"));
            a.setCurso(extrairValorJson(corpoBody, "curso"));
            String semStr = extrairValorJson(corpoBody, "semestre_atual");
            if (semStr != null) {
                try {
                    a.setSemestre_atual(Integer.parseInt(semStr));
                } catch (NumberFormatException ignored) {}
            }
            u = a;
        } else if (tipoEnum == TipoUsuario.PROFESSOR) {
            Professor p = new Professor();
            p.setMatricula_docente(extrairValorJson(corpoBody, "matricula_docente"));
            p.setTitulacao(extrairValorJson(corpoBody, "titulacao"));
            p.setRegimeTrabalho(extrairValorJson(corpoBody, "regime_trabalho"));
            u = p;
        } else if (tipoEnum == TipoUsuario.ADMINISTRATIVO) {
            Administrativo adm = new Administrativo();
            adm.setCargo(extrairValorJson(corpoBody, "cargo"));
            adm.setSetor(extrairValorJson(corpoBody, "setor"));
            adm.setRamal(extrairValorJson(corpoBody, "ramal"));
            u = adm;
        } else {
            u = new Usuario();
        }

        u.setCpf(cpf);
        u.setNome(nome);
        u.setSobrenome(sobrenome);
        u.setEmail_institucional(email);
        u.setSenha_hash(senhaCriptografada);
        u.setData_nascimento(dataNascimento);
        u.setTipo_usuario(tipoEnum);
        u.setId_departamento(idDepartamento);

        String telefone = extrairValorJson(corpoBody, "numero_telefone");
        String tipoTelefone = extrairValorJson(corpoBody, "tipo_telefone");

        usuarioDAO.cadastrar(u, telefone, tipoTelefone);
        enviarResposta(exchange, 201, "{\"mensagem\": \"Usuário cadastrado com sucesso!\"}");
    }

    private void tratarPut(HttpExchange exchange) throws SQLException, IOException {
        String corpoBody = lerCorpoRequisicao(exchange);

        String idStr = extrairValorJson(corpoBody, "id_usuario");
        if (idStr == null) {
            String query = exchange.getRequestURI().getQuery();
            Integer idQuery = obterParametroInt(query, "id");
            if (idQuery != null) {
                idStr = String.valueOf(idQuery);
            }
        }

        if (idStr == null) {
            enviarResposta(exchange, 400, "{\"erro\": \"ID do usuário é obrigatório para atualização\"}");
            return;
        }

        int idUsuario;
        try {
            idUsuario = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            enviarResposta(exchange, 400, "{\"erro\": \"ID do usuário deve ser um número inteiro\"}");
            return;
        }

        Usuario usuarioExistente = usuarioDAO.buscarPorId(idUsuario);
        if (usuarioExistente == null) {
            enviarResposta(exchange, 404, "{\"erro\": \"Usuário não encontrado para atualização\"}");
            return;
        }

        String cpf = extrairValorJson(corpoBody, "cpf");
        String nome = extrairValorJson(corpoBody, "nome");
        String sobrenome = extrairValorJson(corpoBody, "sobrenome");
        String email = extrairValorJson(corpoBody, "email_institucional");
        String senha = extrairValorJson(corpoBody, "senha");
        if (senha == null) {
            senha = extrairValorJson(corpoBody, "senha_hash");
        }
        String dataNascStr = extrairValorJson(corpoBody, "data_nascimento");
        String tipoStr = extrairValorJson(corpoBody, "tipo_usuario");
        String idDepStr = extrairValorJson(corpoBody, "id_departamento");

        if (cpf != null) usuarioExistente.setCpf(cpf);
        if (nome != null) usuarioExistente.setNome(nome);
        if (sobrenome != null) usuarioExistente.setSobrenome(sobrenome);
        if (email != null) usuarioExistente.setEmail_institucional(email);
        if (senha != null && !senha.trim().isEmpty()) {
            usuarioExistente.setSenha_hash(criptografarSenha(senha));
        }
        if (dataNascStr != null) {
            try {
                usuarioExistente.setData_nascimento(Date.valueOf(dataNascStr));
            } catch (IllegalArgumentException e) {
                enviarResposta(exchange, 400, "{\"erro\": \"Data de nascimento deve estar no formato AAAA-MM-DD\"}");
                return;
            }
        }
        if (tipoStr != null) {
            try {
                usuarioExistente.setTipo_usuario(TipoUsuario.valueOf(tipoStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                enviarResposta(exchange, 400, "{\"erro\": \"Tipo de usuário inválido\"}");
                return;
            }
        }
        if (idDepStr != null) {
            try {
                usuarioExistente.setId_departamento(Integer.parseInt(idDepStr));
            } catch (NumberFormatException e) {
                enviarResposta(exchange, 400, "{\"erro\": \"ID do departamento deve ser um número inteiro\"}");
                return;
            }
        }

        usuarioDAO.atualizar(usuarioExistente);
        enviarResposta(exchange, 200, "{\"mensagem\": \"Usuário atualizado com sucesso!\"}");
    }

    private void tratarDelete(HttpExchange exchange) throws SQLException, IOException {
        String query = exchange.getRequestURI().getQuery();
        Integer id = obterParametroInt(query, "id");

        if (id == null) {
            String corpoBody = lerCorpoRequisicao(exchange);
            String idStr = extrairValorJson(corpoBody, "id_usuario");
            if (idStr != null) {
                try {
                    id = Integer.parseInt(idStr);
                } catch (NumberFormatException e) {
                    enviarResposta(exchange, 400, "{\"erro\": \"ID do usuário deve ser um número inteiro\"}");
                    return;
                }
            }
        }

        if (id != null) {
            Usuario u = usuarioDAO.buscarPorId(id);
            if (u == null) {
                enviarResposta(exchange, 404, "{\"erro\": \"Usuário não encontrado para exclusão\"}");
                return;
            }
            usuarioDAO.deletar(id);
            enviarResposta(exchange, 200, "{\"mensagem\": \"Usuário removido com sucesso!\"}");
        } else {
            enviarResposta(exchange, 400, "{\"erro\": \"ID do usuário não informado\"}");
        }
    }

    private String criptografarSenha(String senha) {
        if (senha == null) return null;
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

    private String usuarioParaJson(Usuario u) {
        return String.format(
                "{\"id_usuario\": %d, \"cpf\": \"%s\", \"nome\": \"%s\", \"sobrenome\": \"%s\", \"email_institucional\": \"%s\", \"data_nascimento\": \"%s\", \"tipo_usuario\": \"%s\", \"id_departamento\": %d}",
                u.getId_usuario(),
                u.getCpf() != null ? u.getCpf() : "",
                u.getNome() != null ? u.getNome() : "",
                u.getSobrenome() != null ? u.getSobrenome() : "",
                u.getEmail_institucional() != null ? u.getEmail_institucional() : "",
                u.getData_nascimento() != null ? u.getData_nascimento().toString() : "",
                u.getTipo_usuario() != null ? u.getTipo_usuario().name() : "",
                u.getId_departamento());
    }

    private String listaParaJson(List<Usuario> lista) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lista.size(); i++) {
            sb.append(usuarioParaJson(lista.get(i)));
            if (i < lista.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}