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

public class TestClient {

    private String hostname;
    private int porto;
    private Socket socket;

    public TestClient(String hostname, int porto) {
        this.hostname = hostname;
        this.porto = porto;
    }
    
    public void runTest(String username, String password) throws IOException, InterruptedException {
    	
    	socket = new Socket(this.hostname, this.porto);
    	
    	//criar canais de leitura/escrita no socket
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        
        String response;	//string para ler a resposta do servidor

        // Receber mensagem de boas vindas
        while ((response = in.readLine()) != null) {
            if (response.equals("3. Sair")) {
                break;
            }
        }
        
        // Fazer login
        
        out.write("1");
        out.newLine();
        out.flush();
        
        response = in.readLine();
        
        out.write(username);
        out.newLine();
        out.flush();
        
        response = in.readLine();
        
        out.write(password);
        out.newLine();
        out.flush();
        
        response = in.readLine();
        
        // Esperar por mensagem a dizer "Selecione o seu jogador (de 1 a 30). Tem 30 segundos para o fazer!"
        
        // Para testar, vamos escolher um jogador aleatório por segundo durante 20 segundos
        
        int changes = 0;
        
        while (changes < 20) {
        	
        	out.write(ThreadLocalRandom.current().nextInt(1, 30 + 1));
        	out.newLine();
        	out.flush();
        	
        	TimeUnit.SECONDS.sleep(1);
        }
    }

    public void start(String username, String password) {

        try {
        	
        	this.runTest(username, password);

        } catch (UnknownHostException e) {
            System.out.println("ERRO: Server doesn't exist!");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
