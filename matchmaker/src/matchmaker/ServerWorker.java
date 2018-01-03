package matchmaker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import static java.lang.Thread.sleep;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerWorker implements Runnable {

    private Socket socket;
    private int id;
    private Game game = null; // podia ser lol, FM, cs:go
    private User loggedUser = null;
    private Play activePlay = null;
    private Server server;

    public ServerWorker(Socket socket, int id, Server server, Game game) {
        this.socket = socket;
        this.id = id;
        this.server = server;
        this.game = game;
    }

    @Override
    public void run() {

        try {
            //criar canais de leitura/escrita no socket
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            // -----------------------------------------------------------------
            // Boas vindas e menu de entrada.
            out.write("BEM-VINDO AO MELHOR JOGO DE SEMPRE!\n1. Iniciar sessao\n2. Registar-se\n3. Sair");
            out.newLine();
            out.flush();
            System.out.println("Worker-" + id + " sent Welcome Message.");

            if (!launchMenu(in, out)) {
                System.out.println("\nWorker-" + id + " > Client disconnected. Connection is closed.\n");

                //fechar sockets
                socket.shutdownOutput();
                socket.shutdownInput();
                socket.close();
                return;
            }
            System.out.println("\nWorker-" + id + " > FIM MENU ENTRADA\n");
            // FIM MENU ENTRADA

            // -----------------------------------------------------------------
            // Inserir o jogador numa partida e dar a conhecer o seu BufferedWriter para as mensagens em team no lobby de seleção
            System.out.println("\nWorker-" + id + " > ARRANGING PLAY\n");
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
            // TODO -> tentar eliminar esta espera ativa
            while (!activePlay.isPlayFull()) {
                // System.out.println("\nWorker-" + id + " > Informed user  " + loggedUser.getUsername() + " WAITING");
            }

            // Informar ao jogador que pode escolher o seu campeão
            System.out.println("Worker-" + id + " > ASKED to CHOOSE CHAMPION.");
            out.write("Selecione o seu jogador (de 1 a 30). Tem 30 segundos para o fazer!");
            out.newLine();
            out.flush();

            // Iniciar escolha de campeão
            //Criar lobby thread para receber e transmitir escolhas
            Thread lobby = new Thread(new ChampionsLobby(id, in, out, loggedUser, activePlay));
            lobby.start();

            // Esperar os 30 segundos
            for (int i = 1; i <= 5; i++) {
                waitFor(5);
                System.out.println("Passaram " + (i * 5) + " segundos.");
                out.write("Passaram " + (i * 5) + " segundos.");
                out.newLine();
                out.flush();
            }
            // Parar a thread do lobby porque o tempo acabou
            out.write("Terminou o tempo!");
            out.newLine();
            out.flush();
            lobby.interrupt();
            System.out.println("\nWorker-" + id + " > LOBBY STOPPED.");

            // Ver se todos os jogadores escolheram
            if (activePlay.allChampionsPicked()) {
                System.out.println("\nWorker-" + id + " > Players selected champions!");
                out.write("Jogando...");
                out.newLine();
                out.flush();
                if (activePlay.didTeam1Win()) {
                    out.write("Ganhou a EQUIPA 1!");
                    out.newLine();
                    out.flush();
                    activePlay.rankingUpdate(loggedUser, 1);
                    out.write("O seu novo ranking é: " + loggedUser.getRanking());
                    out.newLine();
                    out.flush();
                } else {
                    out.write("Ganhou a EQUIPA 2!");
                    out.newLine();
                    out.flush();
                    activePlay.rankingUpdate(loggedUser, 2);
                    out.write("O seu novo ranking é: " + loggedUser.getRanking());
                    out.newLine();
                    out.flush();
                }
            } else {
                System.out.println("\nWorker-" + id + " > Champions selection failed!");
                out.write("Alguém não escolheu o seu campeão. Até já!");
                out.newLine();
                out.flush();
            }

            System.out.println("\nWorker-" + id + " > ASKED TO QUIT!");
            out.write("Escreva quit para sair.");
            out.newLine();
            out.flush();

            // codigo standard usado pelos profs
            /*
            while ((line = in.readLine()) != null) {
                System.out.println("\nWorker-" + id + " > Received message from client: " + line);
                out.write(line);
                out.newLine();
                out.flush();
                System.out.println("Worker-" + id + " > Broadcasted with: " + line);
            }
             */
            System.out.println("\nWorker-" + id + " > Client disconnected. Connection is closed.\n");

            //fechar sockets
            socket.shutdownOutput();
            socket.shutdownInput();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException ex) {
            Logger.getLogger(ServerWorker.class.getName()).log(Level.SEVERE, null, ex);
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

            // Pede password
            if (((username = in.readLine()) != null)) {
            	
                System.out.println("\nWorker-" + id + " > Received message from client: " + username);
                out.write("Insira a sua password.");
                out.newLine();
                out.flush();
                System.out.println("Worker-" + id + " asked for PASSWORD.");
                
                // Confirma password e informa entrada
                if (((password = in.readLine()) != null) && this.server.login(username, password)) {
                	
                	// TODO - Não podemos guardar referência a User, tem de ser String do username
                	// Isto pode causar problemas de concorrência, uma vez que estamos a colocar a informação
                	// sobre se o utilizador está logado ou não em mais que um sítio (só devia estar no Server, this.server)
                	// No entanto acho que não está a causar problemas neste momento.
                	
                	loggedUser = new User(username, password); // User fica logado na thread
                	
                    System.out.println("\nWorker-" + id + " > Received message from client: " + password);
                    out.write("LOGIN BEM SUCEDIDO");
                    out.newLine();
                    out.flush();
                    System.out.println("Worker-" + id + " accepted user.");

                    return true;
                    
                } // Informa pass errada
                else {
                	
                    out.write("USERNAME ou PASSWORD errados! > 1. Iniciar sessao || 2. Registar utilizador || 3. Sair");
                    out.newLine();
                    out.flush();
                    System.out.println("Worker-" + id + " WRONG PASSWORD OR USERNAME.");

                    return false;
                    
                }
            } // Informa username nulo
            else {
                out.write("USERNAME nulo! > 1. Iniciar sessao || 2. Registar utilizador || 3. Sair");
                out.newLine();
                out.flush();
                System.out.println("Worker-" + id + " NULL USERNAME.");

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

            if (((username = in.readLine()) != null)) {
                out.write("Insira a sua password.");
                out.newLine();
                out.flush();
                System.out.println("Worker-" + id + " asked for NEW PASSWORD.");
                
                // Confirma password e informa entrada
                if (((password = in.readLine()) != null)) {
                	
                    User newUser = new User(username, password);
                    this.server.register(username, newUser);
                    
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
                System.out.println("Worker-" + id + " made ACTIVE PLAY an EXHISTANT one.");

            }

            System.out.println("Worker-" + id + " num of players: " + activePlay.getPlayers());
        } // criar novo jogo com ranking do primeiro jogador e adicionar o jogador à play. Fazer o jogo available para outros jogadores. activePlay é essa.
        else {

            activePlay = new Play(player.getRanking());
            activePlay.winningTeamRandom(); // definir equipa vencedora por random
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

    private void waitFor(int i) throws InterruptedException {
        sleep(i * 1000);
    }

    private boolean launchMenu(BufferedReader in, BufferedWriter out) throws IOException {

        String line;

        // Receber e tratar resposta do menu de entrada
        while ((line = in.readLine()) != null) {
            System.out.println("\nWorker-" + id + " > Received message from client: " + line);

            // Recebe pedido de inicio de sessao
            if (line.equals("1")) {
                // Log in correu bem, portanto saímos do ciclo de menu
                if (logIn(in, out)) {
                    return true; // fizemos logIn e menu acaba com sucesso
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

                    return false; // pedido de quit, continuar = false
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

        return false; // fechados os canais do outro lado, vamos sair

    }

    class ChampionSelection implements Callable<String> {

        @Override
        public String call() throws Exception {
            System.out.println("ANTES");
            Thread.sleep(4000); // Just to demo a long running task of 4 seconds.
            System.out.println("DEPOIS");
            return "Ready!";
        }
    }

}
