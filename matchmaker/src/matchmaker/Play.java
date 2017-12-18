/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package matchmaker;

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
    // talvez insira array para guarder selecionadas
    private int ranking;
    private boolean full;

    public Play(int rank) {
        team1 = new HashMap<>();
        team2 = new HashMap<>();
        ranking = rank;
        full = false;
    }

    public int getRanking() {

        return ranking;
    }

    // disabled for testing purposes
    public void addPlayer(User player) {

        if (team1.size() < 5) {

            team1.put(player, "NENHUM");

        } else if (team2.size() < 5) {

            team2.put(player, "NENHUM");

        } else {

            full = true;

        }

    }

    public boolean isPlayFull() {

        return full;
    }

}
