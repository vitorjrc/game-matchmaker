package matchmaker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadLocalRandom;

public class TestClient implements Runnable {

    private String hostname;
    private int porto;
    private Socket socket;
    private String username;
    private String password;

    public TestClient(String hostname, int porto, String username, String password) {
        this.hostname = hostname;
        this.porto = porto;
        this.username = username;
    	this.password = password;
    }
    
	@Override
    public void run() {
		
		System.out.println("Client " + this.username + " starting.");
    	
    	try {
    		
			socket = new Socket(this.hostname, this.porto);
    	
	    	//criar canais de leitura/escrita no socket
	        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
	        
	        String response; //string para ler a resposta do servidor
	
	        // Receber mensagem de boas vindas
	        while ((response = in.readLine()) != null) {
	            if (response.equals("3. Sair")) {
	                break;
	            }
	        }
	        
	        // Fazer login
	        
	        TimeUnit.SECONDS.sleep(ThreadLocalRandom.current().nextInt(1, 3 + 1));
	        
	        out.write("1");
	        out.newLine();
	        out.flush();
	        
	        response = in.readLine();
	        
	        TimeUnit.SECONDS.sleep(ThreadLocalRandom.current().nextInt(1, 3 + 1));
	        
	        out.write(this.username);
	        out.newLine();
	        out.flush();
	        
	        response = in.readLine();

	        TimeUnit.SECONDS.sleep(ThreadLocalRandom.current().nextInt(1, 3 + 1));
	        
	        out.write(this.password);
	        out.newLine();
	        out.flush();
	        
	        response = in.readLine();
	        
	        // Esperar por mensagem a dizer "Selecione o seu jogador (de 1 a 30). Tem 30 segundos para o fazer!"
	        
	        // Para testar, vamos escolher um jogador aleatório durante 20 segundos
	        
	        int changes = 0;
	        
	        while (changes < 20) {
	        	
	        	out.write(String.valueOf(ThreadLocalRandom.current().nextInt(1, 30 + 1)));
	        	out.newLine();
	        	out.flush();
	        	
	        	TimeUnit.SECONDS.sleep(ThreadLocalRandom.current().nextInt(3, 7 + 1));
	        }
	        
    	} catch (UnknownHostException e) {
    		e.printStackTrace();
    	} catch (IOException e) {
    		e.printStackTrace();
    	} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
}
