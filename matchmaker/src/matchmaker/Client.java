package matchmaker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

    private String hostname;
    private int porto;
    private Socket socket;

    public Client(String hostname, int porto) {
        this.hostname = hostname;
        this.porto = porto;
    }

    public void clientStart() {

        try {
            System.out.println("#### CLIENT ####");
            System.out.println("> Connecting to server...");
            socket = new Socket(this.hostname, this.porto);
            System.out.println("> Connection accepted!");

            //criar canais de leitura/escrita no socket
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            //criar canal de leitura do stdin
            BufferedReader systemIn = new BufferedReader(new InputStreamReader(System.in));

            String userInput; 	//string para ler o input do utilizador
            String response;	//string para ler a resposta do servidor
            System.out.print("$ ");

            // Receber mensagem de boas vindas
            while ((response = in.readLine()) != null) {
                System.out.println(response);
                if (response.equals("3. Sair")) {
                    break;
                }
            }
            System.out.print("\n$ ");

            // Menu inicial
            while ((userInput = systemIn.readLine()) != null && !userInput.equals("quit") && !response.equals("ENTER para continuar.")) {
                out.write(userInput);
                out.newLine();
                out.flush();

                response = in.readLine();
                System.out.println("> Received response from server: " + response);
            }

            // Receber informações da play em que entrou
            while ((response = in.readLine()) != null) {
                System.out.println(response);
                if (response.equals("Prepare-se para escolher o seu campeão!")) {
                    break;
                }
            }
            System.out.print("\n$ ");

            //Criar listener thread para receber mensagens de outros utilizadores
            Thread listener = new Thread(new ClientListener(in));
            listener.start();

            // Falar com o servidor para escolher personagem
            while ((userInput = systemIn.readLine()) != null && !userInput.equals("quit")) {
                out.write(userInput);
                out.newLine();
                out.flush();

                response = in.readLine();
                System.out.println("> Received response from server: " + response);
                System.out.print("\n$ ");
            }

            //fechar sockets
            socket.shutdownOutput();
            socket.shutdownInput();
            socket.close();

        } catch (UnknownHostException e) {
            System.out.println("ERRO: Server doesn't exist!");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Client c = new Client("127.0.0.1", 12345);
        c.clientStart();
    }
}
