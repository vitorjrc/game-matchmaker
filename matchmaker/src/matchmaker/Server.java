package matchmaker;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class Server {

    private ServerSocket servsocket;
    private int porto;
    private HashMap<String, User> users = new HashMap<String, User>();
    private Game overwatch = new Game();

    public Server(int porto) {
        this.porto = porto;
    }

    public void startServer() {
        try {
            System.out.println("#### SERVER ####");
            this.servsocket = new ServerSocket(this.porto);
            int workerCounter = 1; //contador de ids para worker threads

            while (true) {
                System.out.println("ServerMain > Server is running waiting for a new connection...");
                Socket socket = servsocket.accept();
                System.out.println("ServerMain > Connection received! Create worker thread to handle connection.");

                ServerWorker sw = new ServerWorker(socket, workerCounter++, this, overwatch);
                new Thread(sw).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /*
     * Used to log users in, so that only the main server thread has access to the users hashmap
     */
    public synchronized boolean login(String username, String password) {
    	
    	if (!this.users.containsKey(username)) {
    		return false;
    	}
    	
    	if (this.users.get(username).getPassword().equals(password)) {
    		
    		this.users.get(username).setLoggedIn(true);
    		return true;
    	}
    	
    	return false;
    }
    
    /*
     * Logout, unsynchronized because there's no race to logout, only to login
     */
    public void logout(String username) {
    	
    	if (this.users.containsKey(username)) {
    		this.users.get(username).setLoggedIn(false);
    	}
    }
    
    public synchronized boolean register(String username, User user) {
    	
    	if (!this.users.containsKey(username)) {
    		
    		this.users.put(username, user);
    	}
    	
    	return false;
    }
    
    public synchronized int getPlayerRanking(String username) {
    	
    	return this.users.get(username).getRanking();
    }
    
    public synchronized void setPlayerRanking(String username, int ranking) {
    	this.users.get(username).setRanking(ranking);
    }

    public synchronized void increasePlayerRanking(String username) {
    	this.users.get(username).increaseRanking();
    }
    
    public synchronized void decreasePlayerRanking(String username) {
    	this.users.get(username).decreaseRanking();
    }

    public static void main(String[] args) {
    	
        // Testar users
    	
    	HashMap<String, User> testUsers = new HashMap<>();
    	
        testUsers.put("um", new User("um", "123456"));
        testUsers.put("dois", new User("dois", "123456"));
        testUsers.put("tres", new User("tres", "123456"));
        testUsers.put("quatro", new User("quatro", "123456"));
        testUsers.put("cinco", new User("cinco", "123456"));
        testUsers.put("seis", new User("seis", "123456"));
        testUsers.put("sete", new User("sete", "123456"));
        testUsers.put("oito", new User("oito", "123456"));
        testUsers.put("sergio", new User("sergio", "123456"));
        testUsers.put("vitor", new User("vitor", "123456"));
        testUsers.put("marcos", new User("marcos", "123456"));
        testUsers.put("diana", new User("diana", "123456"));
        testUsers.put("zé", new User("zé", "123456"));
        testUsers.put("a", new User("a", "123456"));
        testUsers.put("b", new User("b", "123456"));
        testUsers.put("c", new User("c", "123456"));
        testUsers.put("e", new User("e", "123456"));
        testUsers.put("f", new User("f", "123456"));
        testUsers.put("g", new User("g", "123456"));
        testUsers.put("h", new User("h", "123456"));
        testUsers.put("i", new User("i", "123456"));
        testUsers.put("j", new User("j", "123456"));
        testUsers.put("k", new User("k", "123456"));
        testUsers.put("l", new User("l", "123456"));
        testUsers.put("m", new User("m", "123456"));
        testUsers.put("n", new User("n", "123456"));
        testUsers.put("o", new User("o", "123456"));
        testUsers.put("p", new User("p", "123456"));
        testUsers.put("q", new User("q", "123456"));
        testUsers.put("r", new User("r", "123456"));
        testUsers.put("s", new User("s", "123456"));
        testUsers.put("t", new User("t", "123456"));
        testUsers.put("u", new User("u", "123456"));
        testUsers.put("v", new User("v", "123456"));
        testUsers.put("w", new User("w", "123456"));
        testUsers.put("x", new User("x", "123456"));
        testUsers.put("y", new User("y", "123456"));
        testUsers.put("z", new User("z", "123456"));
        //

        Server s = new Server(12345);
        
        Runnable test = () -> {

	    	// Testar com todos os users
			
			try {
				TimeUnit.SECONDS.sleep(2); // Esperar que o servidor ligue
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	    
	        ArrayList<Thread> clients = new ArrayList<>();
	        
	        for (User u : testUsers.values()) {
	        	
	        	s.register(u.getUsername(), u);
	        	
	        	Runnable temp = new TestClient("127.0.0.1", 12345, u.getUsername(), u.getPassword());
	        	        	
	        	Thread t = new Thread(temp);
	        	clients.add(t);
	        	t.start();
	        }
        };
        
        new Thread(test).start(); // Start test runnable which generates TestClients
        s.startServer();          // Listen for client connections
    }
}
