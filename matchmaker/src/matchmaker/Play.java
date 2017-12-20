/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package matchmaker;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Vitor Castro
 */
// NAO ESTA A PERMITIR CONCORRENCIA AINDA
public class Play {

    // O arraylist guarda o user e o boneco que o mesmo escolheu
    private Map<User, String> team1 = null;
    private Map<User, String> team2 = null;
    private Map<String, BufferedWriter> clients = null; // utilizador associado ao seu out do server para ele
    // talvez insira array para guarder selecionadas
    private int ranking;
    private int players;

    public Play(int rank) {
        team1 = new HashMap<>();
        team2 = new HashMap<>();
        clients = new HashMap<>();
        ranking = rank;
        players = 0;
    }

    public int getRanking() {

        return ranking;
    }

    // disabled for testing purposes
    public void addPlayer(User player) {

        if (team1.size() < 5) {

            team1.put(player, "NENHUM");

        } else {

            team2.put(player, "NENHUM");

        }

        players++;

    }

    public boolean isPlayFull() {

        return (players >= 10);
    }

    public int getPlayers() {

        return players;
    }

    public void launchChampionSelection() {

    }

    public synchronized boolean registerClientOut(String nick, BufferedWriter writer) {

        if (!clients.containsKey(nick)) {
            clients.put(nick, writer);
            return true;
        }

        return false;
    }

    public synchronized void multicast(String userSender, String msg) {
        msg = userSender + ": " + msg;
        for (String user : clients.keySet()) {
            if (!user.equals(userSender)) {
                try {
                    BufferedWriter bw = clients.get(user);
                    bw.write(msg);
                    bw.newLine();
                    bw.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
