import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Conexao {
    private final Socket socket;
    private final BufferedReader entrada;
    private final PrintWriter saida;
    private volatile String nomeUsuario;
    private volatile boolean ativo = true;

    public Conexao(Socket socket) throws IOException {
        this.socket = socket;
        this.entrada = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        // autoflush desligado: enviar() faz print + flush manual, sempre com "\n"
        this.saida = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), false);
    }

    public boolean estaRegistrado() {
        return nomeUsuario != null && ativo;
    }

    public void iniciar() {
        Thread thread = new Thread(() -> {
            try {
                enviar("SERVIDOR|Digite seu nome usando JOIN:nome");

                String mensagem;
                while (ativo && (mensagem = entrada.readLine()) != null) {
                    if (mensagem.trim().isEmpty()) {
                        continue;
                    }
                    processarMensagem(mensagem.trim());
                }
            } catch (IOException e) {
                if (ativo) {
                    System.out.println("Erro na conexão: " + e.getMessage());
                }
            } finally {
                fechar();
            }
        }, "conexao-" + socket.getPort());
        thread.start();
    }

    /** Pode ser chamado por varias threads ao mesmo tempo (broadcast). */
    public synchronized void enviar(String mensagem) {
        saida.print(mensagem + "\n");
        saida.flush();
        if (saida.checkError()) {      // PrintWriter engole IOException; checamos aqui
            ativo = false;
            Servidor.clientes.remove(this);
            try { socket.close(); } catch (IOException ignorado) { }
        }
    }

    private void processarMensagem(String mensagem) {
        if (mensagem.startsWith("JOIN:")) {
            String nome = mensagem.substring("JOIN:".length()).trim().replace("|", "_");
            if (nome.isEmpty()) {
                enviar("SERVIDOR|Nome invalido. Use JOIN:nome");
                return;
            }
            if (nomeUsuario != null) {
                enviar("SERVIDOR|Voce ja esta registrado como " + nomeUsuario);
                return;
            }

            this.nomeUsuario = nome;   // registra ANTES de avisar, para o autor tambem receber
            System.out.println("Usuario " + nome + " entrou no chat.");
            Servidor.enviarParaTodos("SERVIDOR|" + nome + " entrou no chat.");
            enviar("SERVIDOR|Conectado ao chat. Digite sua mensagem.");
            return;
        }

        if (mensagem.equalsIgnoreCase("QUIT")) {
            if (nomeUsuario != null) {
                Servidor.enviarParaTodosExceto(this, "SERVIDOR|" + nomeUsuario + " saiu do chat.");
            }
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
        System.out.println(pacote + "  -> repassando para " + (contarDestinatarios()) + " cliente(s)");
        Servidor.enviarParaTodosExceto(this, pacote);
    }

    private int contarDestinatarios() {
        int n = 0;
        for (Conexao c : Servidor.clientes) {
            if (c != this && c.estaRegistrado()) n++;
        }
        return n;
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

        System.out.println("Cliente desconectado" + (nomeUsuario != null ? " (" + nomeUsuario + ")" : "") + ".");
        System.out.println("Clientes conectados: " + Servidor.clientes.size());
    }
}
