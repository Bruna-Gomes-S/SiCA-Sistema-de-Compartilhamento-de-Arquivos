import os
import socket

# Configurações para saber onde se conectar
HOST = '127.0.0.1'
PORT = 65432
BUFFER_SIZE = 4096
DOWNLOAD_DIR = 'client_downloads' 

if not os.path.exists(DOWNLOAD_DIR):
    os.makedirs(DOWNLOAD_DIR)

# Conecta ao servidor TCP
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock.connect((HOST, PORT))
print("Conectado ao servidor com sucesso!")

while True:
    print("\n1. Listar arquivos | 2. Enviar arquivo | 3. Baixar arquivo | 4. Sair")
    opcao = input("Escolha uma opção: ").strip()

    # Opção 1: Pedir a lista de arquivos
    if opcao == '1':
        sock.send("LIST".encode('utf-8')) # Envia a palavra "LIST"
        resposta = sock.recv(BUFFER_SIZE).decode('utf-8') # Recebe o texto com os nomes
        print("Arquivos no servidor:\n" + resposta)

    # Opção 2: Enviar um arquivo do seu PC para o servidor
    elif opcao == '2':
        caminho = input("Caminho do arquivo local: ").strip()
        if os.path.exists(caminho):
            nome = os.path.basename(caminho)
            sock.send(f"UPLOAD {nome}".encode('utf-8')) # Avisa que vai enviar
            
            sock.recv(BUFFER_SIZE) # Espera o "OK" do servidor
            tamanho = os.path.getsize(caminho)
            sock.send(str(tamanho).encode('utf-8')) # Manda o tamanho total
            sock.recv(BUFFER_SIZE) # Espera o "READY" do servidor

            # Abre o arquivo local e envia pedaço por pedaço
            with open(caminho, 'rb') as f:
                while pedaco := f.read(BUFFER_SIZE):
                    sock.send(pedaco)
            
            print("Envio concluído!")

    # Opção 3: Baixar um arquivo do servidor para o seu PC
    elif opcao == '3':
        nome = input("Nome do arquivo para baixar: ").strip()
        sock.send(f"DOWNLOAD {nome}".encode('utf-8')) # Pede o arquivo
        
        status = sock.recv(BUFFER_SIZE).decode('utf-8')
        if status == "FOUND":
            sock.send("READY".encode('utf-8'))
            tamanho = int(sock.recv(BUFFER_SIZE).decode('utf-8')) # Pega o tamanho total
            sock.send("READY".encode('utf-8'))

            caminho_salvar = os.path.join(DOWNLOAD_DIR, nome)
            bytes_recebidos = 0

            # Recebe os dados em pedaços e escreve na sua pasta local
            with open(caminho_salvar, 'wb') as f:
                while bytes_recebidos < tamanho:
                    pedaco = sock.recv(BUFFER_SIZE)
                    f.write(pedaco)
                    bytes_recebidos += len(pedaco)
            
            print("Download concluído com sucesso!")
        else:
            print("Arquivo não encontrado no servidor.")

    # Opção 4: Sair do programa
    elif opcao == '4':
        sock.send("EXIT".encode('utf-8'))
        break

sock.close() # Desconecta do servidor