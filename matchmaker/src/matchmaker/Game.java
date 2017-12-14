/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package matchmaker;

import java.util.HashMap;

/**
 *
 * @author Vitor Castro
 */
public class Game {

    private static HashMap<Integer, Play> games = null;

    public Game() {

        games = new HashMap<Integer, Play>(); // Integer é o rank do jogo
    }

    public boolean isPlayStarted(int rank) {

        // meter a consultar map
    }
}
