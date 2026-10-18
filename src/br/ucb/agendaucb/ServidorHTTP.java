package br.ucb.agendaucb;

import com.sun.net.httpserver.HttpServer;

import br.ucb.agendaucb.controller.AgendamentoController;
import br.ucb.agendaucb.controller.LoginController;
import br.ucb.agendaucb.controller.PredioController;
import br.ucb.agendaucb.controller.RecursoController;
import br.ucb.agendaucb.controller.SalaController;
import br.ucb.agendaucb.controller.TipoAgendamentoController;
import br.ucb.agendaucb.controller.UsuarioController;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;

public class ServidorHTTP {

    private static final int PORTA = 8080;

    public static void iniciarServidor() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORTA), 0);

        // 1. Rotas de API (Controllers RESTful)
        server.createContext("/api/recursos", new RecursoController());
        server.createContext("/api/predios", new PredioController());
        server.createContext("/api/salas", new SalaController());
        server.createContext("/api/usuarios", new UsuarioController());
        server.createContext("/api/login", new LoginController());
        server.createContext("/api/agendamentos", new AgendamentoController());
        server.createContext("/api/tipos-agendamento", new TipoAgendamentoController());

        // 2. Rota Estática Universal (Entrega qualquer arquivo da pasta web/)
        server.createContext("/", exchange -> {
            String uri = exchange.getRequestURI().getPath();

            if (uri.equals("/")) {
                uri = "/index.html";
            }

            Path caminhoArquivo = Paths.get("web" + uri);

            if (Files.exists(caminhoArquivo) && !Files.isDirectory(caminhoArquivo)) {
                String contentType = resolverContentType(uri);

                byte[] resposta = Files.readAllBytes(caminhoArquivo);
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, resposta.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(resposta);
                }
            } else {
                String erro = "{\"erro\": \"Arquivo não encontrado: " + uri + "\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                exchange.sendResponseHeaders(404, erro.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(erro.getBytes());
                }
            }
        });

        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();

        System.out.println("==================================================");
        System.out.println(" Servidor AgendaUCB rodando em http://localhost:" + PORTA);
        System.out.println(" - Painel Principal     : http://localhost:" + PORTA + "/index.html");
        System.out.println(" - API Recursos         : http://localhost:" + PORTA + "/api/recursos");
        System.out.println(" - API Prédios          : http://localhost:" + PORTA + "/api/predios");
        System.out.println(" - API Salas            : http://localhost:" + PORTA + "/api/salas");
        System.out.println(" - API Usuários         : http://localhost:" + PORTA + "/api/usuarios");
        System.out.println(" - API Login            : http://localhost:" + PORTA + "/api/login");
        System.out.println(" - API Agendamentos     : http://localhost:" + PORTA + "/api/agendamentos");
        System.out.println(" - API Tipos Agendamento: http://localhost:" + PORTA + "/api/tipos-agendamento");
        System.out.println("==================================================");
    }

    private static String resolverContentType(String uri) {
        if (uri.endsWith(".html")) return "text/html; charset=UTF-8";
        if (uri.endsWith(".css")) return "text/css; charset=UTF-8";
        if (uri.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (uri.endsWith(".json")) return "application/json; charset=UTF-8";
        if (uri.endsWith(".png")) return "image/png";
        if (uri.endsWith(".jpg") || uri.endsWith(".jpeg")) return "image/jpeg";
        if (uri.endsWith(".svg")) return "image/svg+xml";
        if (uri.endsWith(".ico")) return "image/x-icon";
        return "text/plain; charset=UTF-8";
    }
}