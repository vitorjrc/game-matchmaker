/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package matchmaker;

import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Vitor Castro
 */
// POR ENQUANTO ESTÁ COM CONCURRENT HASHMAP MAS DEPOIS TEM QUE SE IMPLEMENTAR MÉTODOS SYNCHRONIZED
public class Game {

    // tem os jogos atualmente ativos, por ranking. faz-se remove quando é finalizado
    private static ConcurrentHashMap<Integer, Play> plays = null;
    // tem os jogos que estão em seleçao ou terminados
    private static ConcurrentHashMap<Integer, Play> closedPlays = null;

    public Game() {

        plays = new ConcurrentHashMap<Integer, Play>(); // Integer é o rank do jogo
        closedPlays = new ConcurrentHashMap<Integer, Play>();
    }

    /*
    Retorna a play com aquele rank
     */
    public Play getPlay(int rank) {

        return plays.get(rank); // não fazemos clone porque queremos mesmo modificar este objeto no ServerWorker, e não uma cópia
    }

    /*
    Devolve o rank da play em que o jogador pode entrar. -1 caso seja necessário criar um novo jogo
     */
    public int isPlayStarted(int rank) {

        if (plays.containsKey(rank)) {
            return rank;
        } else if (plays.containsKey(rank - 1)) {
            return rank - 1;
        } else if (plays.containsKey(rank + 1)) {
            return rank + 1;
        } else {
            return (-1);
        }
    }

    /*
    Insere nova play no jogo
     */
    public void launchNewPlay(int rank, Play newPlay) {

        plays.put(rank, newPlay);
    }

    /*
    Remove play finalizada do jogo
     */
    public void endPlay(int rank) {

        plays.remove(rank);
    }
}
