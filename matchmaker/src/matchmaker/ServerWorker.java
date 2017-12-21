package matchmaker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerWorker implements Runnable {

    private Socket socket;
    private int id;
    private ConcurrentHashMap<String, User> users = null;
    private Game game = null; // podia ser lol, FM, cs:go
    private User loggedUser = null;
    private Play activePlay = null;

    public ServerWorker(Socket socket, int id, ConcurrentHashMap<String, User> users, Game game) {
        this.socket = socket;
        this.id = id;
        this.users = users; // Passamos direto, fica com o acesso aberto ao array de utilizadores do servidor
        this.game = game;
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
            // Inserir o jogador numa partida e dar a conhecer o seu BufferedWriter para as mensagens em team no lobby de seleção
            arrangePlay(loggedUser, out);

            // Informar login para o username X, jogo em que entrou e dizer para se preparar para escolher
            out.write("Login efetuado, " + loggedUser.getUsername() + ".");
            out.newLine();
            out.write("Entrou numa partida de ranking: " + Integer.toString(activePlay.getRanking()) + ".");
            out.newLine();
            out.write("A sua partida começa dentro de momentos. Aguarde...");
            out.newLine();
            out.flush();
            System.out.println("\nWorker-" + id + " > Informed login to user: " + loggedUser.getUsername()
                    + ", playing in game ranked: " + Integer.toString(activePlay.getRanking()));
            // FIM DE PREPARAÇÃO DE PARTIDA NOVA

            // Esperar que play esteja cheia
            while (!activePlay.isPlayFull()) {
                System.out.println("\nWorker-" + id + " > Informed user  " + loggedUser.getUsername() + " WAITING");
            }

            // Informar ao jogador que pode escolher o seu campeão
            System.out.println("Worker-" + id + " > ASKED to CHOOSE CHAMPION with: " + line);
            out.write("Selecione o seu jogador (de 1 a 30). Tem 30 segundos para o fazer!");
            out.newLine();
            out.flush();

            // 30 segundos para fazer a escolha
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<String> future = executor.submit(new ChampionSelection());

            try {
                System.out.println("Started..");
                System.out.println(future.get(3, TimeUnit.SECONDS));
                System.out.println("Finished!");
            } catch (TimeoutException e) {
                future.cancel(true);
                System.out.println("Terminated!");
            } catch (InterruptedException ex) {
                Logger.getLogger(ServerWorker.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(ServerWorker.class.getName()).log(Level.SEVERE, null, ex);
            }

            executor.shutdownNow();
            // -----------------------------------------------------------------

            // Escolher campeão
            while ((line = in.readLine()) != null) {
                System.out.println("\nWorker-" + id + " > Received message from client: " + line);
                if (isNumber(line) && (Integer.parseInt(line) > 0) && (Integer.parseInt(line) < 31)) {
                    if (activePlay.chooseChampion(loggedUser, line)) { // já mete o campeão associado ao player
                        activePlay.teamcast(loggedUser, line); // diz à equipa que aquele jogador escolheu aquele campeao
                        System.out.println("Worker-" + id + " > Player " + loggedUser.getUsername() + " choosed champion: " + line);
                        System.out.println("Worker-" + id + " > Teamcasted with: " + line);
                        out.write("Campeão selecionado! Caso queira mudar, basta inserir outro número.");
                        out.newLine();
                        out.flush();
                    } else {
                        System.out.println("\nWorker-" + id + " > Informed UNAVAILABLE CHAMPION to: " + loggedUser.getUsername());
                        out.write("Campeão indisponível! Escolha outro.");
                        out.newLine();
                        out.flush();
                    }
                } else {
                    System.out.println("\nWorker-" + id + " > Informed INVALID CHAMPION to: " + loggedUser.getUsername());
                    out.write("Esse campeão ainda não nasceu! Escolha outro.");
                    out.newLine();
                    out.flush();
                }
            }

            /* CODIGO PARA CHAT
            while ((line = in.readLine()) != null) {
                System.out.println("\nWorker-" + id + " > Received message from client: " + line);
                activePlay.teamcast(loggedUser, line);
                System.out.println("Worker-" + id + " > Broadcasted with: " + line);
            }
             */
            // codigo standard usado pelos profs
            while ((line = in.readLine()) != null) {
                System.out.println("\nWorker-" + id + " > Received message from client: " + line);
                out.write(line);
                out.newLine();
                out.flush();
                System.out.println("Worker-" + id + " > Broadcasted with: " + line);
            }
            // FIM DE ESCOLHA DE JOGADORES

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
                    out.write("LOGIN BEM SUCEDIDO");
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

    private void arrangePlay(User player, BufferedWriter out) {

        int rank = game.isPlayAvailable(player.getRanking());

        // isPlayStarted retorna -1 caso não haja uma play disponível, ou o valor do rank da play disponível
        if (rank >= 0) {

            activePlay = game.getPlay(rank);
            // disabled for testing purposes - add that player to the game selectec
            activePlay.addPlayer(loggedUser); // ja da o update do numero de jogadores
            activePlay.registerClientOut(loggedUser, out); // para que depois seja possível obter o BufferedWriter para comunicação
            // check if the teams are full and the game ready to start (in case this is the last player, which is the one that will launch the choice menu)
            if (activePlay.isPlayFull()) {

                game.startPlay(activePlay); // tira a play actual das availables e dá como iniciada a mesma

            }
            System.out.println("Worker-" + id + " made ACTIVE PLAY an EXHISTANT one.");
            System.out.println("Worker-" + id + " num of players: " + activePlay.getPlayers());
        } // criar novo jogo com ranking do primeiro jogador e adicionar o jogador à play. Fazer o jogo available para outros jogadores. activePlay é essa.
        else {

            activePlay = new Play(player.getRanking());
            // disabled for testing purposes
            activePlay.addPlayer(loggedUser);
            activePlay.registerClientOut(loggedUser, out); // para que depois seja possível obter o BufferedWriter para comunicação
            game.launchNewPlay(activePlay.getRanking(), activePlay);
            System.out.println("Worker-" + id + " created a NEW PLAY.");
            System.out.println("Worker-" + id + " num of players: " + activePlay.getPlayers());
        }
    }

    /*
    * Check if the input is readable by the parseInt. If not, we just return false so the system will not fail
     */
    private boolean isNumber(String line) {

        boolean amIValid = false;

        try {
            Integer.parseInt(line);
            amIValid = true;
        } catch (NumberFormatException e) {
        }

        return amIValid;
    }

    class ChampionSelection implements Callable<String> {

        @Override
        public String call() throws Exception {
            System.out.println("ANTES");
            Thread.sleep(2000); // Just to demo a long running task of 4 seconds.
            System.out.println("DEPOIS");
            return "Ready!";
        }
    }

}
