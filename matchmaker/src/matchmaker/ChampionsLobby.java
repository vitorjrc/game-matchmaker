/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package matchmaker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 *
 * @author Vitor Castro
 */
public class ChampionsLobby implements Runnable {

    int id;
    BufferedReader in = null;
    BufferedWriter out = null;
    String loggedUser = null; // Username of logged user
    Play activePlay = null;

    public ChampionsLobby(int workerId, BufferedReader input, BufferedWriter output, String user, Play play) {

        id = workerId;
        in = input;
        out = output;
        loggedUser = user;
        activePlay = play;
    }

    /**
     * Used to continuously read the messages broadcasted
     */
    @Override
    public void run() {

        try {
            String line;

            while ((line = in.readLine()) != null) {
                System.out.println("\nWorker-" + id + " > Received message from client: " + line);
                if (isNumber(line) && (Integer.parseInt(line) > 0) && (Integer.parseInt(line) < 31)) {
                    if (activePlay.chooseChampion(loggedUser, line)) { // já mete o campeão associado ao player
                        activePlay.teamcast(loggedUser, line); // diz à equipa que aquele jogador escolheu aquele campeao
                        System.out.println("Worker-" + id + " > Player " + loggedUser + " choosed champion: " + line);
                        System.out.println("Worker-" + id + " > Teamcasted with: " + line);
                        out.write("Campeão selecionado! Caso queira mudar, basta inserir outro número.");
                        out.newLine();
                        out.flush();
                    } else {
                        System.out.println("\nWorker-" + id + " > Informed UNAVAILABLE CHAMPION to: " + loggedUser);
                        out.write("Campeão indisponível! Escolha outro.");
                        out.newLine();
                        out.flush();
                    }
                } else {
                    System.out.println("\nWorker-" + id + " > Informed INVALID CHAMPION to: " + loggedUser);
                    out.write("Esse campeão ainda não nasceu! Escolha outro.");
                    out.newLine();
                    out.flush();
                }
            }
        } catch (IOException e) {
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

}
