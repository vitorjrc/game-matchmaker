package matchmaker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;


public class ServerWorker implements Runnable{

	private Socket socket;
	private int id;
	
	public ServerWorker (Socket socket, int id){
		this.socket = socket;		
		this.id = id;
	}
	

@Override
public void run() {
	
	try{
		//criar canais de leitura/escrita no socket
		BufferedReader in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                
                // Boas vindas e menu de entrada.
                out.write("BEM-VINDO AO MELHOR JOGO DE SEMPRE!\n1. Iniciar sessao\n2. Registar-se\n3. Sair");
                out.newLine();
		out.flush();
                System.out.println("Worker-"+id+" sended Welcome Message.");

		String line;	//string para ler mensagens do cliente
		while((line = in.readLine()) != null){
			System.out.println("\nWorker-"+id+" > Received message from client: " + line);
			out.write(line);
			out.newLine();
			out.flush();
			System.out.println("Worker-"+id+" > Reply with: " + line);
		}
		
		System.out.println("\nWorker-"+id+" > Client disconnected. Connection is closed.\n");

		//fechar sockets
		socket.shutdownOutput();
		socket.shutdownInput();
		socket.close();
		
	} catch (IOException e) {
		e.printStackTrace();
	}
	
}
	
		
}
