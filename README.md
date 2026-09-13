# 📁 SiCA - Sistema de Compartilhamento de Arquivos

## 1. O Que o Programa Faz
* Conecta dois computadores (Cliente e Servidor) para trocar arquivos de forma segura usando o protocolo TCP.
* O cliente pode **listar**, **enviar** ou **baixar** arquivos.

---

## 2. Como Funciona o Servidor (`server.py`)
* **Conexão**: Fica ligado esperando o cliente aparecer.
* **Atendimento**: Funciona em loop para responder a todos os pedidos.
* **LIST**: Mostra os arquivos salvos na pasta.
* **UPLOAD**: Recebe um arquivo do cliente e salva na pasta.
* **DOWNLOAD**: Pega um arquivo da pasta e envia para o cliente.

---

## 3. Como Funciona o Cliente (`cliente.py`)
* **Conexão**: Liga para o IP e porta do servidor.
* **Menu**: Mostra as opções na tela para o usuário escolher.
* **Envio em Pedaços**: Divide os arquivos em partes pequenas de 4 KB para não travar a memória do computador.

---

## 4. Passo a Passo da Troca de Dados
1. O cliente se conecta ao servidor.
2. O cliente pede o que quer (ex: "Baixar imagem").
3. O servidor confirma se tem o arquivo e o tamanho dele.
4. O arquivo é enviado pedaço por pedaço até terminar.

---

## 📝 Comunicação por Sockets
* Protocolo TCP: Garante que os arquivos cheguem completos e sem erros de transmissão.
* Transferência por Chunks: Os arquivos são divididos em blocos de 4 KB durante o envio e recepção, otimizando o uso de memória.
