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

    // O arraylist guarda o user e o boneco que o mesmo escolheu (boneco = 0 se ainda não escolheu)
    private Map<User, Integer> team1 = null;
    private Map<User, Integer> team2 = null;
    private Map<User, BufferedWriter> clients = null; // utilizador associado ao seu out do server para ele
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

            team1.put(player, 0);

        } else {

            team2.put(player, 0);

        }

        players++;

    }

    public boolean isPlayFull() {

        return (players >= 10);
    }

    public int getPlayers() {

        return players;
    }

    /*
    * Updates the champion of a given player if that champion is not choosed already
     */
    public boolean chooseChampion(User player, String selected) {

        Integer selectedChampion = new Integer(selected);

        // Verificar se esse campeão já não está escolhido
        for (Integer jogador : team1.values()) {
            if (selectedChampion.equals(jogador)) {
                return false;
            }
        }

        if (team1.containsKey(player)) {
            // Verificar se esse campeão já não está escolhido
            for (Integer jogador : team1.values()) {
                if (selectedChampion.equals(jogador)) {
                    return false;
                } else {
                    continue;
                }
            }
            // como não foi selecionado, pode pôr
            team1.put(player, selectedChampion);
        } else if (team2.containsKey(player)) {
            // Verificar se esse campeão já não está escolhido
            for (Integer jogador : team2.values()) {
                if (selectedChampion.equals(jogador)) {
                    return false;
                } else {
                    continue;
                }
            }
            // como não foi selecionado, pode pôr
            team2.put(player, selectedChampion);
        }

        return true;

    }

    public synchronized boolean registerClientOut(User player, BufferedWriter writer) {

        if (!clients.containsKey(player)) {
            clients.put(player, writer);
            return true;
        }

        return false;
    }

    /*
    * Broadcast a certain message for the team of the user.
     */
    public synchronized void teamcast(User userSender, String msg) {
        msg = userSender.getUsername() + " escolheu o campeão " + msg;
        for (User player : clients.keySet()) {
            if (!player.getUsername().equals(userSender.getUsername()) // se não é o próprio utilizador
                    && // se são da mesma equipa 1
                    ((team1.containsKey(userSender) && team1.containsKey(player))
                    || // se são da mesma equipa
                    (team2.containsKey(userSender) && team2.containsKey(player)))) {
                try {
                    BufferedWriter bw = clients.get(player);
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
