import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Conexao {
    private final Socket socket;
    private String nomeUsuario;
    private BufferedReader entrada;
    private PrintWriter saida;
    private boolean ativo = true;

    public Conexao(Socket socket) {
        this.socket = socket;
    }

    public void iniciar() {
        Thread thread = new Thread(() -> {
            try {
                entrada = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)
                );
                saida = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8),
                        true
                );

                enviar("SERVIDOR|Digite seu nome usando JOIN:nome");

                String mensagem;
                while (ativo && (mensagem = entrada.readLine()) != null) {
                    if (mensagem.trim().isEmpty()) {
                        continue;
                    }
                    processarMensagem(mensagem.trim());
                }
            } catch (IOException e) {
                System.out.println("Erro na conexão: " + e.getMessage());
            } finally {
                fechar();
            }
        });

        thread.start();
    }

    public void enviar(String mensagem) {
        if (saida != null) {
            saida.println(mensagem);
        }
    }

    private void processarMensagem(String mensagem) {
        if (mensagem.startsWith("JOIN:")) {
            String nome = mensagem.substring("JOIN:".length()).trim();
            if (nome.isEmpty()) {
                enviar("SERVIDOR|Nome invalido. Use JOIN:nome");
                return;
            }

            this.nomeUsuario = nome;
            System.out.println("Usuario " + nome + " entrou no chat.");
            Servidor.enviarParaTodos("SERVIDOR|" + nome + " entrou no chat.");
            enviar("SERVIDOR|Conectado ao chat. Digite sua mensagem.");
            return;
        }

        if (mensagem.equalsIgnoreCase("QUIT")) {
            String nomeSaida = (nomeUsuario != null) ? nomeUsuario : "Usuario";
            Servidor.enviarParaTodos("SERVIDOR|" + nomeSaida + " saiu do chat.");
            ativo = false;
            return;
        }

        if (nomeUsuario == null) {
            enviar("SERVIDOR|Registre seu nome antes de enviar mensagens: JOIN:nome");
            return;
        }

        String conteudo = mensagem;
        if (mensagem.startsWith("MSG:")) {
            conteudo = mensagem.substring("MSG:".length()).trim();
        }

        if (conteudo.isEmpty()) {
            enviar("SERVIDOR|Mensagem vazia.");
            return;
        }

        String pacote = "MSG|" + nomeUsuario + "|" + conteudo;
        System.out.println(pacote);
        Servidor.enviarParaTodosExceto(this, pacote);
    }

    private void fechar() {
        ativo = false;
        Servidor.clientes.remove(this);

        try {
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.out.println("Erro ao fechar socket: " + e.getMessage());
        }

        System.out.println("Cliente desconectado.");
        System.out.println("Clientes conectados: " + Servidor.clientes.size());
    }
}