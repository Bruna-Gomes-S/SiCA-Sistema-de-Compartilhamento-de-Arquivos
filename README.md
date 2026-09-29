# 📁 SiCA - Sistema de Compartilhamento de Arquivos

## 1. O Que o Programa Faz
* Conecta dois computadores (Cliente e Servidor) para trocar arquivos de forma segura usando sockets TCP em **Java**.
* O cliente pode **listar**, **enviar** ou **baixar** arquivos do servidor.

---

## 2. Como Funciona o Servidor (`ServidorSiCA.java`)
* **Conexão**: Fica escutando a porta de rede aguardando a conexão do cliente.
* **Atendimento**: Processa as requisições enviadas pelo cliente.
* **LIST**: Lista os arquivos disponíveis na pasta do servidor.
* **SEND**: Recebe um arquivo enviado pelo cliente e salva em disco.
* **GET**: Localiza o arquivo solicitado e envia para o cliente.

---

## 3. Como Funciona o Cliente (`ClienteSiCA.java`)
* **Conexão**: Conecta-se ao IP e porta do servidor.
* **Menu**: Exibe um menu interativo no terminal para o usuário escolher a opção desejada.
* **Envio em Pedaços**: Transmite os arquivos em blocos de 4 KB para não sobrecarregar a memória.

---

## 4. Passo a Passo da Troca de Dados
1. O cliente se conecta ao servidor.
2. O cliente envia o comando desejado (`LIST`, `SEND` ou `GET`).
3. O servidor verifica a solicitação e informa o tamanho do arquivo/status.
4. Os dados são transferidos em blocos de bytes até a conclusão da tarefa.

---

## 📝 Comunicação por Sockets
* **Protocolo TCP**: Garante que os arquivos cheguem completos, em ordem e sem erros de transmissão.
* **Transferência por Chunks**: Uso de buffers de 4 KB durante a leitura e escrita via streams de dados.

---

## 🚀🚀 Como Executar o Projeto

1. **Compilar os ficheiros Java:**
   ```bash
   javac ServidorSiCA.java ClienteSiCA.java
