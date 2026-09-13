import os
import socket


HOST = '127.0.0.1'  
PORT = 65432        
BUFFER_SIZE = 4096  
STORAGE_DIR = 'server_storage' 

# Cria a pasta do servidor se ela ainda não existir
if not os.path.exists(STORAGE_DIR):
    os.makedirs(STORAGE_DIR)

# INICIANDO O SERVIDOR
# Cria o "telefone" (socket) usando o protocolo TCP
server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server_socket.bind((HOST, PORT)) 
server_socket.listen(5)          
print("Servidor pronto e aguardando conexão...")

while True:
    # Aceita a ligação de um cliente que tentou se conectar
    client_socket, client_address = server_socket.accept()
    print(f"Cliente conectado: {client_address}")

    try:
        while True:
            # Recebe a mensagem (comando) enviada pelo cliente
            request = client_socket.recv(BUFFER_SIZE).decode('utf-8')
            if not request:
                break # Se o cliente desligar, sai do loop

            # Separa o comando da informação (ex: "DOWNLOAD foto.png" vira "DOWNLOAD" e "foto.png")
            parts = request.split(' ', 1)
            command = parts[0].upper()
            param = parts[1] if len(parts) > 1 else ""

            # CASO 1: O cliente pediu a lista de arquivos (LIST)
            if command == "LIST":
                arquivos = os.listdir(STORAGE_DIR) # Pega os nomes dos arquivos na pasta
                resposta = "\n".join(arquivos) if arquivos else "Pasta vazia."
                client_socket.send(resposta.encode('utf-8')) # Envia a lista pro cliente

            #  O cliente quer enviar um arquivo para o servidor (UPLOAD)
            elif command == "UPLOAD":
                nome_arquivo = os.path.basename(param)
                caminho_completo = os.path.join(STORAGE_DIR, nome_arquivo)
                
                client_socket.send("OK".encode('utf-8')) # Avisa: "pode mandar"

                # Recebe o tamanho total do arquivo
                tamanho_total = int(client_socket.recv(BUFFER_SIZE).decode('utf-8'))
                client_socket.send("READY".encode('utf-8')) # Avisa: "pronto pra receber os dados"

                # Recebe o arquivo pedaço por pedaço e salva no computador
                bytes_recebidos = 0
                with open(caminho_completo, 'wb') as arquivo:
                    while bytes_recebidos < tamanho_total:
                        pedaco = client_socket.recv(BUFFER_SIZE)
                        arquivo.write(pedaco)
                        bytes_recebidos += len(pedaco)

                client_socket.send("SUCCESS".encode('utf-8')) # Confirma que deu certo

            # O cliente quer baixar um arquivo do servidor (DOWNLOAD)
            elif command == "DOWNLOAD":
                nome_arquivo = os.path.basename(param)
                caminho_completo = os.path.join(STORAGE_DIR, nome_arquivo)

                # Verifica se o arquivo realmente existe na pasta
                if os.path.exists(caminho_completo):
                    client_socket.send("FOUND".encode('utf-8')) # Avisa que achou
                    client_socket.recv(BUFFER_SIZE) # Espera confirmação do cliente

                    # Envia o tamanho do arquivo
                    tamanho = os.path.getsize(caminho_completo)
                    client_socket.send(str(tamanho).encode('utf-8'))
                    client_socket.recv(BUFFER_SIZE) # Espera confirmação do cliente

                    # Lê o arquivo em pedaços e vai enviando pela rede
                    with open(caminho_completo, 'rb') as arquivo:
                        while pedaco := arquivo.read(BUFFER_SIZE):
                            client_socket.send(pedaco)
                else:
                    client_socket.send("NOT_FOUND".encode('utf-8')) # Avisa que não existe

            # O cliente quer fechar o programa (EXIT)
            elif command == "EXIT":
                break

    finally:
        client_socket.close() # Fecha a conexão com esse cliente
