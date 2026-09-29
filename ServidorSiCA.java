import java.io.*;
import java.net.*;

public class ServidorSiCA {
    private static final int PORTA = 12345;
    private static final String DIRETORIO_SERVIDOR = "./servidor_arquivos/";

    public static void main(String[] args) {
        // Cria o diretório do servidor se não existir
        File diretorio = new File(DIRETORIO_SERVIDOR);
        if (!diretorio.exists()) {
            diretorio.mkdirs();
        }

        System.out.println("=== Servidor SiCA iniciado na porta " + PORTA + " ===");

        try (ServerSocket serverSocket = new ServerSocket(PORTA)) {
            while (true) {
                // Aguarda e aceita a conexão de um cliente
                Socket socket = serverSocket.accept();
                System.out.println("Cliente conectado: " + socket.getInetAddress().getHostAddress());

                // Processa a requisição do cliente
                tratarCliente(socket);
            }
        } catch (IOException e) {
            System.err.println("Erro no servidor: " + e.getMessage());
        }
    }

    /**
     * Método responsável por ler o comando enviado pelo cliente e executar a ação correspondente.
     */
    private static void tratarCliente(Socket socket) {
        try (
            DataInputStream entrada = new DataInputStream(socket.getInputStream());
            DataOutputStream saida = new DataOutputStream(socket.getOutputStream())
        ) {
            // Lê o comando enviado pelo cliente
            String comando = entrada.readUTF();

            if (comando.equals("LIST")) {
                listarArquivos(saida);
            } else if (comando.startsWith("SEND")) {
                String nomeArquivo = comando.substring(5);
                receberArquivo(entrada, nomeArquivo);
            } else if (comando.startsWith("GET")) {
                String nomeArquivo = comando.substring(4);
                enviarArquivo(saida, nomeArquivo);
            } else {
                saida.writeUTF("ERRO: Comando não reconhecido.");
            }

        } catch (IOException e) {
            System.err.println("Erro ao tratar cliente: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Erro ao fechar socket: " + e.getMessage());
            }
        }
    }

    /**
     * Lista todos os ficheiros armazenados no diretório do servidor.
     */
    private static void listarArquivos(DataOutputStream saida) throws IOException {
        File pasta = new File(DIRETORIO_SERVIDOR);
        File[] listaArquivos = pasta.listFiles();

        if (listaArquivos == null || listaArquivos.length == 0) {
            saida.writeUTF("Nenhum arquivo disponível no servidor.");
            return;
        }

        StringBuilder lista = new StringBuilder("Ficheiros disponíveis no servidor:\n");
        for (File f : listaArquivos) {
            if (f.isFile()) {
                lista.append("- ").append(f.getName()).append(" (").append(f.length()).append(" bytes)\n");
            }
        }
        saida.writeUTF(lista.toString());
    }

    /**
     * Recebe um ficheiro enviado pelo cliente via stream de bytes.
     */
    private static void receberArquivo(DataInputStream entrada, String nomeArquivo) throws IOException {
        File arquivo = new File(DIRETORIO_SERVIDOR + nomeArquivo);
        long tamanho = entrada.readLong(); // Lê o tamanho do arquivo

        try (FileOutputStream fos = new FileOutputStream(arquivo)) {
            byte[] buffer = new byte[4096];
            int bytesLidos;
            long totalLido = 0;

            while (totalLido < tamanho && (bytesLidos = entrada.read(buffer, 0, (int) Math.min(buffer.length, tamanho - totalLido))) != -1) {
                fos.write(buffer, 0, bytesLidos);
                totalLido += bytesLidos;
            }
        }
        System.out.println("Arquivo '" + nomeArquivo + "' recebido com sucesso.");
    }

    /**
     * Envia um ficheiro do servidor para o cliente.
     */
    private static void enviarArquivo(DataOutputStream saida, String nomeArquivo) throws IOException {
        File arquivo = new File(DIRETORIO_SERVIDOR + nomeArquivo);

        if (!arquivo.exists() || !arquivo.isFile()) {
            saida.writeBoolean(false); // Indica que o ficheiro não existe
            return;
        }

        saida.writeBoolean(true); // Indica que o ficheiro existe
        saida.writeLong(arquivo.length()); // Envia o tamanho do ficheiro

        try (FileInputStream fis = new FileInputStream(arquivo)) {
            byte[] buffer = new byte[4096];
            int bytesLidos;
            while ((bytesLidos = fis.read(buffer)) != -1) {
                saida.write(buffer, 0, bytesLidos);
            }
        }
        saida.flush();
        System.out.println("Arquivo '" + nomeArquivo + "' enviado ao cliente com sucesso.");
    }
}