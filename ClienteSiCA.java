import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ClienteSiCA {
    private static final String IP_SERVIDOR = "127.0.0.1";
    private static final int PORTA = 12345;
    private static final String DIRETORIO_CLIENTE = "./cliente_arquivos/";

    public static void main(String[] args) {
        // Cria a pasta local do cliente se não existir
        File diretorio = new File(DIRETORIO_CLIENTE);
        if (!diretorio.exists()) {
            diretorio.mkdirs();
        }

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== Sistema de Compartilhamento de Arquivos (SiCA) ===");
            System.out.println("1. Listar arquivos no servidor");
            System.out.println("2. Enviar arquivo para o servidor");
            System.out.println("3. Baixar arquivo do servidor");
            System.out.println("4. Sair");
            System.out.print("Escolha uma opção: ");

            int opcao = scanner.nextInt();
            scanner.nextLine(); // Limpar buffer

            if (opcao == 4) {
                System.out.println("Encerrando a aplicação...");
                break;
            }

            switch (opcao) {
                case 1:
                    solicitarLista();
                    break;
                case 2:
                    System.out.print("Digite o nome do arquivo a ser enviado (deve estar em './cliente_arquivos/'): ");
                    String arquivoEnviar = scanner.nextLine();
                    enviarArquivo(arquivoEnviar);
                    break;
                case 3:
                    System.out.print("Digite o nome do arquivo a ser baixado: ");
                    String arquivoBaixar = scanner.nextLine();
                    baixarArquivo(arquivoBaixar);
                    break;
                default:
                    System.out.println("Opção inválida.");
            }
        }
        scanner.close();
    }

    /**
     * Envia o comando 'LIST' ao servidor e exibe a lista retornado.
     */
    private static void solicitarLista() {
        try (
            Socket socket = new Socket(IP_SERVIDOR, PORTA);
            DataOutputStream saida = new DataOutputStream(socket.getOutputStream());
            DataInputStream entrada = new DataInputStream(socket.getInputStream())
        ) {
            saida.writeUTF("LIST");
            String resposta = entrada.readUTF();
            System.out.println("\n" + resposta);
        } catch (IOException e) {
            System.err.println("Erro ao comunicar com o servidor: " + e.getMessage());
        }
    }

    /**
     * Envia um ficheiro local para a pasta do servidor.
     */
    private static void enviarArquivo(String nomeArquivo) {
        File arquivo = new File(DIRETORIO_CLIENTE + nomeArquivo);

        if (!arquivo.exists() || !arquivo.isFile()) {
            System.out.println("Erro: O arquivo não foi encontrado no diretório local (" + DIRETORIO_CLIENTE + ").");
            return;
        }

        try (
            Socket socket = new Socket(IP_SERVIDOR, PORTA);
            DataOutputStream saida = new DataOutputStream(socket.getOutputStream());
            FileInputStream fis = new FileInputStream(arquivo)
        ) {
            saida.writeUTF("SEND " + nomeArquivo);
            saida.writeLong(arquivo.length());

            byte[] buffer = new byte[4096];
            int bytesLidos;
            while ((bytesLidos = fis.read(buffer)) != -1) {
                saida.write(buffer, 0, bytesLidos);
            }
            saida.flush();
            System.out.println("Arquivo '" + nomeArquivo + "' enviado com sucesso!");
        } catch (IOException e) {
            System.err.println("Erro ao enviar arquivo: " + e.getMessage());
        }
    }

    /**
     * Baixa um ficheiro localizado no servidor para a pasta do cliente.
     */
    private static void baixarArquivo(String nomeArquivo) {
        try (
            Socket socket = new Socket(IP_SERVIDOR, PORTA);
            DataOutputStream saida = new DataOutputStream(socket.getOutputStream());
            DataInputStream entrada = new DataInputStream(socket.getInputStream())
        ) {
            saida.writeUTF("GET " + nomeArquivo);

            boolean existe = entrada.readBoolean();
            if (!existe) {
                System.out.println("Erro: O arquivo solicitado não existe no servidor.");
                return;
            }

            long tamanho = entrada.readLong();
            File arquivoDestino = new File(DIRETORIO_CLIENTE + nomeArquivo);

            try (FileOutputStream fos = new FileOutputStream(arquivoDestino)) {
                byte[] buffer = new byte[4096];
                int bytesLidos;
                long totalLido = 0;

                while (totalLido < tamanho && (bytesLidos = entrada.read(buffer, 0, (int) Math.min(buffer.length, tamanho - totalLido))) != -1) {
                    fos.write(buffer, 0, bytesLidos);
                    totalLido += bytesLidos;
                }
            }
            System.out.println("Arquivo '" + nomeArquivo + "' baixado com sucesso em '" + DIRETORIO_CLIENTE + "'!");
        } catch (IOException e) {
            System.err.println("Erro ao baixar arquivo: " + e.getMessage());
        }
    }
}