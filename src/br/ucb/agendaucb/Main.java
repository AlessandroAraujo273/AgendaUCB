package br.ucb.agendaucb;

import java.awt.Desktop;
import java.net.URI;

public class Main {

    public static void main(String[] args) {
        try {
            System.out.println("==================================================");
            System.out.println("            INICIANDO AGENDAUCB                   ");
            System.out.println("==================================================");

            ServidorHTTP.iniciarServidor();

            String url = "http://localhost:8080/index.html";
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            }

        } catch (Exception e) {
            System.err.println("❌ Erro ao iniciar o servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}