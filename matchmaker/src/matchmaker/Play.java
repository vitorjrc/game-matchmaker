/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package matchmaker;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author Vitor Castro
 */
public class Play {

    // O arraylist guarda o username e o boneco que o mesmo escolheu (boneco = 0 se ainda não escolheu)
    private Map<String, Integer> team1 = null;
    private Map<String, Integer> team2 = null;
    private Map<String, BufferedWriter> clients = null; // utilizador associado ao seu out do server para ele
    // talvez insira array para guarder selecionadas
    private int ranking;
    private int players;
    private boolean team1Wins;
    private int maxPlayers;
    private boolean concurrencyVerified; // Usada no fim do jogo para verificar concorrência apenas uma vez

    public Play(int rank) {
        team1 = new HashMap<>();
        team2 = new HashMap<>();
        clients = new HashMap<>();
        ranking = rank;
        players = 0;
        maxPlayers = 10;
        
        this.concurrencyVerified = false;
    }

    public synchronized int getRanking() {

        return ranking;
    }
    
    public int getMaxPlayers() {
    	return this.maxPlayers;
    }
    
    public  Map<String, Integer> getTeam1(){
        return this.team1;
    }
    
    public  Map<String, Integer> getTeam2(){
        return this.team2;
    }
    
    public synchronized int getNumPlayers() {
    	return this.players;
    }

    public synchronized void winningTeamRandom() {

        Random rn = new Random();
        int answer = rn.nextInt(10) + 1; // gera numeros de 1 a 10

        if (answer <= 5) {
            team1Wins = true;
        } else {
            team1Wins = false;
        }

    }

    public synchronized boolean didTeam1Win() {

        return team1Wins;
    }
    
    // Used to know if a player should rank up, together with didTeam1Win()
    public synchronized boolean isPlayerInTeam1(String username) {
    	return this.team1.containsKey(username);
    	
    	// Could check whether player is also on team 2, and throw error if not
    }

    // disabled for testing purposes
    public synchronized void addPlayer(String player) {

        if (team1.size() < 5) {

            team1.put(player, 0);

        } else {

            team2.put(player, 0);

        }

        players++;
        
        this.notifyAll(); // Notify all ServerWorkers waiting for the play to fill up
    }

    public synchronized boolean isPlayFull() {

        return (players >= this.maxPlayers);
    }

    public synchronized int getPlayers() {

        return players;
    }

    /*
    * Updates the champion of a given player if that champion is not choosed already
     */
    public synchronized boolean chooseChampion(String player, String selected) {

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

    public synchronized boolean allChampionsPicked() {

        for (Integer jogador : team1.values()) {
            if (jogador.equals(0)) {
                return false;
            } else {
                continue;
            }
        }

        for (Integer jogador : team2.values()) {
            if (jogador.equals(0)) {
                return false;
            } else {
                continue;
            }
        }

        return true;

    }

    public synchronized boolean registerClientOut(String player, BufferedWriter writer) {

        if (!clients.containsKey(player)) {
            clients.put(player, writer);
            return true;
        }

        return false;
    }

    /*
    * Broadcast a certain message for the team of the user.
     */
    public synchronized void teamcast(String userSender, String msg) {
        msg = userSender + " escolheu o campeão " + msg;
        for (String player : clients.keySet()) {
            if (!player.equals(userSender) // se não é o próprio utilizador
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

    /*
    * Broadcast a certain message for the team of the user.
     */
    public synchronized void broadcast(String msg) {
        for (String player : clients.keySet()) {
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
    
    
    // Testing methods
    
    public void ensureConcurrency() {
    	
    	// Verificar apenas uma vez (todos os jogadores vão tentar verificar a play)
    	synchronized(this) {
    		
    		if (!this.concurrencyVerified) {
    			this.concurrencyVerified = true;
    		} else {
    			return;
    		}
     	}
    	
    	this.checkRepeatedChampions();
    	
    	// Não relacionados a concorrência mas importantes na mesma
    	this.checkPlayerRankings(); 
    	this.checkPlayerNumber();
    }
    
    private void checkRepeatedChampions() {
    	
    	Play play = this;
    	
        // TODO
    	// Este método verifica que não há jogadores na play com champions repetidos
    	// Só é preciso preencher o método, ServerWorker já chama ensureConcurrency()
    	// Se houver faz isto:
    	
    	Set<Integer> champions = new HashSet<Integer>();
    	boolean repeatedChampion = false;
        
        synchronized(play){
            
            for(Integer i : play.getTeam1().values()){
                System.out.println(i);
                // 0 quer dizer que n escolheu champion ainda e só queremos verificar se alguém tem champs repetidos
                 if(i!=0 && !champions.add(i)){
                     repeatedChampion = true;
                 }
            }
           
            champions.clear();
          
            for(Integer i : play.getTeam2().values()){
                if (i!=0 && !champions.add(i)){
                    repeatedChampion = true;
                }
            }
            
           
            if (repeatedChampion) {
    		
    		System.out.println("\nAyayayaya hay uns championes repetidios! madre nos tenga! \nay\nay\nay\nay\nay\n");
            }
        
        }
    	
    }
    
    private void checkPlayerRankings() {
    	
    	Play play = this;
    	
    	boolean nonCompatibleRanking = false;
    	
    	// TODO
    	// Este método verifica que os jogadores da play não têm diferença de rankings > 1 entre si
    	// Só é preciso preencher o método, ServerWorker já chama ensureConcurrency()
    	// Se houver faz isto:
    	
    	if (nonCompatibleRanking) {
    		
    		System.out.println("\nAyayayaya hay un ranking no compatible! que hacemos hombre! \nay\nay\nay\nay\nay\n");
    	}
    }
    
    private void checkPlayerNumber() {
    	
    	Play play = this;
    	
    	if (play.getMaxPlayers() != play.getNumPlayers()) {
    		
    		System.out.println("\nAyayayaya lo jumero de hogadores! lo maximum eres " + String.valueOf(play.getMaxPlayers()) + " pero hay " + String.valueOf(play.getNumPlayers()) + "! \nay\nay\nay\nay\nay\n");
    	}
    }
    
}
