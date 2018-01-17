package matchmaker;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Vitor Castro
 */
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
    
    public void ensureConcurrency(Play play) {
    	
    	this.checkRepeatedChampions(play);
    	
    	// Não relacionados a concorrência mas importantes na mesma
    	this.checkPlayerRankings(play); 
    	this.checkPlayerNumber(play);
    }
    
    private void checkRepeatedChampions(Play play) {
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
    
    private void checkPlayerRankings(Play play) {
    	
    	boolean nonCompatibleRanking = false;
    	
    	// TODO
    	// Este método verifica que os jogadores da play não têm diferença de rankings > 1 entre si
    	// Só é preciso preencher o método, ServerWorker já chama ensureConcurrency()
    	// Se houver faz isto:
    	
    	if (nonCompatibleRanking) {
    		
    		System.out.println("\nAyayayaya hay un ranking no compatible! que hacemos hombre! \nay\nay\nay\nay\nay\n");
    	}
    }
    
    private void checkPlayerNumber(Play play) {
    	
    	if (play.getMaxPlayers() != play.getNumPlayers()) {
    		
    		System.out.println("\nAyayayaya lo jumero de hogadores! lo maximum eres " + String.valueOf(play.getMaxPlayers()) + " pero hay " + String.valueOf(play.getNumPlayers()) + "! \nay\nay\nay\nay\nay\n");
    	}
    }
}
