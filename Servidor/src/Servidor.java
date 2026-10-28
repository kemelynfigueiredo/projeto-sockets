import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Servidor {
    private static final int PORTA = 12354;
    static final List<Conexao> clientes = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        try (ServerSocket servidor = new ServerSocket(PORTA)) {
            System.out.println("Servidor de chat iniciado na porta " + PORTA);
            System.out.println("Aguardando clientes...");

            while (true) {
                Socket socket = servidor.accept();
                Conexao conexao = new Conexao(socket);
                clientes.add(conexao);
                System.out.println(
                        "Cliente conectado: "
                                + socket.getInetAddress().getHostAddress()
                                + ":"
                                + socket.getPort()
                );
                System.out.println("Clientes conectados: " + clientes.size());
                conexao.iniciar();
            }
        } catch (IOException e) {
            System.err.println("Erro ao iniciar o servidor: " + e.getMessage());
        }
    }

    static void enviarParaTodos(String mensagem) {
        for (Conexao cliente : clientes) {
            cliente.enviar(mensagem);
        }
    }

    static void enviarParaTodosExceto(Conexao origem, String mensagem) {
        for (Conexao cliente : clientes) {
            if (cliente != origem) {
                cliente.enviar(mensagem);
            }
        }
    }
}