/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package matchmaker;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Vitor Castro
 */
//Métodos synchronized implementados(todos) TODO-> verificar se há alguns que não seja necessário garantir o lock
public class Game {

    // tem os jogos atualmente ativos, por ranking. faz-se remove quando é finalizado
    private static HashMap<Integer, Play> plays = null;
    // tem os jogos que estão em seleçao ou terminados
    private static HashMap<Integer, Play> closedPlays = null;

    public Game() {

        plays = new HashMap<Integer, Play>(); // Integer é o rank do jogo
        closedPlays = new HashMap<Integer, Play>();
    }

    /*
    Retorna a play com aquele rank
     */
    public synchronized Play getPlay(int rank) {

        return plays.get(rank); // não fazemos clone porque queremos mesmo modificar este objeto no ServerWorker, e não uma cópia
    }

    /*
    Devolve o rank da play em que o jogador pode entrar. -1 caso seja necessário criar um novo jogo
     */
    public synchronized int isPlayAvailable(int rank) {

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
    Remove o jogo dos jogos em espera e mete-o nos jogos a jogar (inclui o processo de seleção)
     */
    public synchronized void startPlay(Play play) {

        closedPlays.put(play.getRanking(), play);
        plays.remove(play.getRanking());

    }

    /*
    Insere nova play no jogo
     */
    public synchronized void launchNewPlay(int rank, Play newPlay) {

        plays.put(rank, newPlay);
    }

    /*
    Remove play finalizada do jogo
     */
    public synchronized void endPlay(int rank) {

        plays.remove(rank);
    }
}
