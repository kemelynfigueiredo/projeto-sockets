# Projeto Sockets

Aplicação cliente-servidor utilizando sockets TCP. O servidor, desenvolvido em Java, atende até **3 clientes simultaneamente** e disponibiliza operações matemáticas e o envio de uma imagem em Base64.

## Funcionalidades

* Divisão, adição e subtração de dois números;
* Solicitação de uma imagem disponibilizada pelo servidor;
* Cliente desenvolvido em Java;
* Cliente desenvolvido em Python;
* Comunicação entre clientes e servidor utilizando sockets TCP.

## Requisitos

* Java (JDK) instalado;
* Python 3 instalado, caso utilize o cliente Python;
* Visual Studio Code para abrir e executar os projetos.

## Como executar

O projeto está dividido em três pastas: `Servidor`, `Cliente-Java` e `Cliente-Python`. Abra cada pasta em uma janela separada do VS Code.

### 1. Iniciar o servidor

Abra a pasta `Servidor` no VS Code. No terminal, execute:

```powershell
cd src
javac Servidor.java Conexao.java
java Servidor
```

Mantenha o terminal do servidor aberto durante a execução dos clientes.

O servidor utiliza a porta `12354` e disponibiliza a imagem armazenada no arquivo `imagem_servidor.png`.

Se aparecer a mensagem `Endereço já em uso`, significa que há um processo antigo do servidor ainda rodando. Para encerrar:

```bash
lsof -i :12354
fuser -k 12354/tcp
```

### 2. Iniciar o cliente Java

Abra a pasta `Cliente-Java` em outra janela do VS Code. No terminal, execute:

```powershell
cd src
javac Cliente.java
java Cliente
```

### 3. Iniciar o cliente Python

Abra a pasta `Cliente-Python` em outra janela do VS Code. No terminal, execute:

```powershell
python Cliente.py
```

O servidor deve estar em execução antes de iniciar os clientes.

## Menu do cliente

| Opção | Ação                                  |
| ----- | ------------------------------------- |
| `1`   | Divide o primeiro número pelo segundo |
| `2`   | Soma dois números                     |
| `3`   | Subtrai o segundo número do primeiro  |
| `4`   | Solicita a imagem ao servidor         |
| `0`   | Encerra a conexão                     |

Ao escolher a opção `4`, a imagem recebida é salva no diretório de execução do cliente, com o nome definido no código.

## Estrutura do projeto

```text
Sockets/
├── Servidor/
│   └── src/
│       ├── Servidor.java
│       ├── Conexao.java
│       └── imagem_servidor.png
│
├── Cliente-Java/
│   └── src/
│       └── Cliente.java
│
└── Cliente-Python/
    └── Cliente.py
```

## Testar em outra máquina

Por padrão, os clientes utilizam o endereço `127.0.0.1`, que representa a própria máquina. Para executar um cliente em outro computador, é necessário substituir esse endereço pelo **IP da máquina onde o servidor está em execução**.

No cliente Java, altere o endereço nesta linha:

```java
new Socket("127.0.0.1", 12354)
```

No cliente Python, altere nesta linha:

```python
socket.create_connection(("127.0.0.1", 12354))
```

Por exemplo, se o IP do computador servidor for `192.168.1.100`, utilize esse endereço nos dois clientes:

```java
new Socket("192.168.1.100", 12354)
```

```python
socket.create_connection(("192.168.1.100", 12354))
```

O endereço acima é apenas um exemplo. Utilize o IP real da máquina que executa o servidor.

Para que a comunicação funcione, os computadores devem ter conectividade entre si, e a porta `12345` precisa estar liberada no firewall da máquina servidor.

## Observações

* O servidor deve permanecer em execução para que os clientes possam se conectar.
* O servidor aceita até 3 clientes simultaneamente.
* Os clientes Java e Python podem ser executados em janelas separadas do VS Code.
* A comunicação utiliza sockets TCP e a porta `12354`.
* Ao executar tudo na mesma máquina, mantenha o endereço `127.0.0.1`.
* Para executar os clientes em outra máquina, utilize o IP do computador servidor.
