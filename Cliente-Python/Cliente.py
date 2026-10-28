import socket
import threading
import os

# Troque aqui pelo IP do computador que roda o Servidor (ex.: "192.168.0.10")
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
    """Thread que so le do socket (igual ao 'receptor' do Cliente.java)."""
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
                # flush=True: aparece na hora mesmo em IDE/saida redirecionada
                print(formatar_mensagem(linha), flush=True)

    print("Conexao encerrada pelo servidor.", flush=True)
    os._exit(0)  # encerra mesmo com o input() da thread principal bloqueado


def main():
    print(f"Conectando em {HOST}:{PORT} ...", flush=True)
    try:
        sock = socket.create_connection((HOST, PORT), timeout=5)
        sock.settimeout(None)
    except OSError as e:
        print(f"Nao foi possivel conectar ao servidor {HOST}:{PORT}: {e}")
        return

    with sock:
        nome = input("Digite seu nome: ").strip()
        while not nome:
            nome = input("Nome invalido. Digite seu nome: ").strip()

        threading.Thread(target=receber, args=(sock,), daemon=True).start()

        sock.sendall(f"JOIN:{nome}\n".encode("utf-8"))
        print("Conectado ao servidor. Digite sua mensagem e 'SAIR' para encerrar.", flush=True)

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
