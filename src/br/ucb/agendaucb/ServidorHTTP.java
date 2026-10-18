package br.ucb.agendaucb;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class ServidorHTTP {

    private static final int PORTA = 8080;

    public static void iniciarServidor() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORTA), 0);

        // Rota de teste temporária
        server.createContext("/", exchange -> {
            String resposta = "{\"status\": \"Servidor AgendaUCB online!\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, resposta.getBytes().length);

            OutputStream os = exchange.getResponseBody();
            os.write(resposta.getBytes());
            os.close();
        });

        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();

        System.out.println("Servidor ativo em http://localhost:" + PORTA);
    }
}