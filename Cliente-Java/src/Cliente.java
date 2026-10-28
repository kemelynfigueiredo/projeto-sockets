import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Cliente {
    // Troque aqui pelo IP do computador que roda o Servidor (ex.: "192.168.0.10")
    private static final String HOST = "127.0.0.1";
    private static final int PORTA = 12354;

    public static void main(String[] args) {
        try (Scanner teclado = new Scanner(System.in)) {
            System.out.println("Conectando em " + HOST + ":" + PORTA + " ...");
            Socket clienteSocket = new Socket(HOST, PORTA);

            BufferedReader entradaServidor = new BufferedReader(
                    new InputStreamReader(clienteSocket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter saida = new PrintWriter(
                    new OutputStreamWriter(clienteSocket.getOutputStream(), StandardCharsets.UTF_8), true);

            System.out.print("Digite seu nome: ");
            String nome = teclado.nextLine().trim();
            while (nome.isEmpty()) {
                System.out.print("Nome invalido. Digite seu nome: ");
                nome = teclado.nextLine().trim();
            }

            // Thread que recebe: lê do socket e imprime, independente do teclado
            Thread receptor = new Thread(() -> {
                try {
                    String mensagem;
                    while ((mensagem = entradaServidor.readLine()) != null) {
                        if (!mensagem.trim().isEmpty()) {
                            System.out.println(formatarMensagem(mensagem.trim()));
                        }
                    }
                    System.out.println("Conexão encerrada pelo servidor.");
                } catch (IOException e) {
                    // socket fechado pelo proprio cliente ao sair: normal
                }
                System.exit(0); // desbloqueia o teclado.nextLine() da thread principal
            }, "receptor");
            receptor.setDaemon(true);
            receptor.start();

            saida.println("JOIN:" + nome);
            System.out.println("Conectado ao servidor. Digite sua mensagem e 'SAIR' para encerrar.");

            while (teclado.hasNextLine()) {
                String texto = teclado.nextLine();
                if (texto.equalsIgnoreCase("SAIR")) {
                    saida.println("QUIT");
                    break;
                }
                if (!texto.trim().isEmpty()) {
                    saida.println("MSG:" + texto);
                }
            }
            clienteSocket.close();
        } catch (IOException e) {
            System.out.println("Nao foi possivel conectar ao servidor: " + e.getMessage());
        }
    }

    private static String formatarMensagem(String mensagem) {
        String[] partes = mensagem.split("\\|", 3);

        if (partes.length == 3 && "MSG".equals(partes[0])) {
            return partes[1] + ": " + partes[2];
        }
        if (partes.length >= 2 && "SERVIDOR".equals(partes[0])) {
            return "[Servidor] " + partes[1];
        }
        return mensagem;
    }
}
