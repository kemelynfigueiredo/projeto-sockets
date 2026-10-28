import socket
import threading
import os

HOST = "127.0.0.1"
PORT = 12354


def formatar_mensagem(mensagem: str) -> str:
    partes = mensagem.split("|", 2)
    if len(partes) == 3 and partes[0] == "MSG":
        return f"{partes[1]}: {partes[2]}"
    if len(partes) >= 2 and partes[0] == "SERVIDOR":
        return f"[Servidor] {partes[1]}"
    return mensagem


def receber(sock: socket.socket):
    buffer = ""
    while True:
        try:
            dados = sock.recv(4096)
        except OSError:
            break
        if not dados:
            break

        buffer += dados.decode("utf-8", errors="replace")
        while "\n" in buffer:
            linha, buffer = buffer.split("\n", 1)
            linha = linha.strip()
            if linha:
                print(f"\n{formatar_mensagem(linha)}")

    print("\nConexao encerrada pelo servidor.")
    os._exit(0)  # encerra mesmo com o input() bloqueado


def main():
    try:
        sock = socket.create_connection((HOST, PORT))
    except OSError as e:
        print(f"Nao foi possivel conectar ao servidor: {e}")
        return

    with sock:
        nome = input("Digite seu nome: ").strip()
        while not nome:
            nome = input("Nome invalido. Digite seu nome: ").strip()

        sock.sendall(f"JOIN:{nome}\n".encode("utf-8"))

        threading.Thread(target=receber, args=(sock,), daemon=True).start()

        print("Conectado ao servidor. Digite sua mensagem e 'SAIR' para encerrar.")
        try:
            while True:
                texto = input().strip()
                if texto.upper() == "SAIR":
                    sock.sendall(b"QUIT\n")
                    break
                if texto:
                    sock.sendall(f"MSG:{texto}\n".encode("utf-8"))
        except (KeyboardInterrupt, EOFError):
            pass


if __name__ == "__main__":
    main()