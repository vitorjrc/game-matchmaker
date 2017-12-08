package matchmaker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ServerWorker implements Runnable {

    private Socket socket;
    private int id;
    private ConcurrentHashMap<String, User> users = null;
    private User loggedUser = null;

    public ServerWorker(Socket socket, int id, ConcurrentHashMap<String, User> users) {
        this.socket = socket;
        this.id = id;
        this.users = users; // Passamos direto, fica com o acesso aberto ao array de utilizadores do servidor
    }

    @Override
    public void run() {

        try {
            //criar canais de leitura/escrita no socket
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            String line;	//string para ler mensagens do cliente
            String username;
            String password;

            // -----------------------------------------------------------------
            // Boas vindas e menu de entrada.
            out.write("BEM-VINDO AO MELHOR JOGO DE SEMPRE!\n1. Iniciar sessao\n2. Registar-se\n3. Sair");
            out.newLine();
            out.flush();
            System.out.println("Worker-" + id + " sended Welcome Message.");

            // Receber e tratar resposta do menu de entrada
            while ((line = in.readLine()) != null) {
                System.out.println("\nWorker-" + id + " > Received message from client: " + line);

                // Recebe pedido de inicio de sessao
                if (line.equals("1")) {
                    // Log in correu bem, portanto saímos do ciclo de menu
                    if (logIn(in, out)) {
                        break;
                    } else {
                        continue;
                    }
                }

                // Recebe pedido de inscricao
                if (line.equals("2")) {
                    register(in, out);
                    continue;
                }

                // Recebe pedido de saida
                if (line.equals("3")) {
                    // Pede que insira "quit"
                    out.write("Escreva 'quit' para sair.");
                    out.newLine();
                    out.flush();
                    System.out.println("Worker-" + id + " asked for QUIT COMMAND.");
                    if ((line = in.readLine()) != null && line.equals("quit")) {
                        break; // ja esta feita a parte do menu de entrada
                    } else {
                        out.write("1. Iniciar sessao || 2. Registar utilizador || 3. Sair");
                        out.newLine();
                        out.flush();
                        System.out.println("Worker-" + id + " showed MENU.");
                    }
                } else { // Caso algo falhou, mostra menu de novo
                    out.write("1. Iniciar sessao || 2. Registar utilizador || 3. Sair");
                    out.newLine();
                    out.flush();
                    System.out.println("Worker-" + id + " showed MENU.");
                }

            }
            // FIM MENU ENTRADA

            // -----------------------------------------------------------------
            // Escolher jogo para o utilizador atual
            while ((line = in.readLine()) != null) {
                System.out.println("\nWorker-" + id + " > Received message from client: " + line);
                out.write(line);
                out.newLine();
                out.flush();
                System.out.println("Worker-" + id + " > Reply with: " + line);
            }

            System.out.println("\nWorker-" + id + " > Client disconnected. Connection is closed.\n");

            //fechar sockets
            socket.shutdownOutput();
            socket.shutdownInput();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private boolean logIn(BufferedReader in, BufferedWriter out) {

        try {

            String username, password;

            // Pede que insira username
            out.write("Insira o seu username.");
            out.newLine();
            out.flush();
            System.out.println("Worker-" + id + " asked for USERNAME.");
            // Se existir pede password
            if (((username = in.readLine()) != null) && users.containsKey(username)) {
                System.out.println("\nWorker-" + id + " > Received message from client: " + username);
                out.write("Insira a sua password.");
                out.newLine();
                out.flush();
                System.out.println("Worker-" + id + " asked for PASSWORD.");
                // Confirma password e informa entrada
                if (((password = in.readLine()) != null) && password.equals(users.get(username).getPassword())) {
                    System.out.println("\nWorker-" + id + " > Received message from client: " + password);
                    out.write("Logando...");
                    out.newLine();
                    out.flush();
                    loggedUser = users.get(username); // User fica logado na thread
                    System.out.println("Worker-" + id + " accepted user.");

                    return true;
                } // Informa pass errada
                else {
                    out.write("PASSWORD errada! > 1. Iniciar sessao || 2. Registar utilizador || 3. Sair");
                    out.newLine();
                    out.flush();
                    System.out.println("Worker-" + id + " WRONG PASSWORD.");

                    return false;
                }
            } // Informa username inexistente
            else {
                out.write("USERNAME não existe! > 1. Iniciar sessao || 2. Registar utilizador || 3. Sair");
                out.newLine();
                out.flush();
                System.out.println("Worker-" + id + " INEXISTENT USERNAME.");

                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void register(BufferedReader in, BufferedWriter out) {

        try {

            String username, password;

            // Pede que insira username
            out.write("Insira o username da sua nova conta.");
            out.newLine();
            out.flush();
            System.out.println("Worker-" + id + " asked for NEW USERNAME.");
            // Se estiver disponivel, pede password
            if (((username = in.readLine()) != null) && !users.containsKey(username)) {
                out.write("Insira a sua password.");
                out.newLine();
                out.flush();
                System.out.println("Worker-" + id + " asked for NEW PASSWORD.");
                // Confirma password e informa entrada
                if (((password = in.readLine()) != null)) {
                    User newUser = new User(username, password);
                    users.put(username, newUser);
                    out.write("Username: " + username + " Password: " + password + " criado. > 1. Iniciar sessao || 2. Registar utilizador || 3. Sair");
                    out.newLine();
                    out.flush();
                    System.out.println("Worker-" + id + " created NEW USER.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
