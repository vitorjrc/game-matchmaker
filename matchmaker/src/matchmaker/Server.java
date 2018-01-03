package matchmaker;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

public class Server {

    private ServerSocket servsocket;
    private int porto;
    private static HashMap<String, User> users = new HashMap<String, User>();
    private Game overwatch = new Game();

    private ReentrantLock lockServer = new ReentrantLock();
    
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

                ServerWorker sw = new ServerWorker(socket, workerCounter++, users, overwatch, lockServer);
                new Thread(sw).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Server s = new Server(12345);

        // Testar users
        users.put("um", new User("um", "123456"));
        users.put("dois", new User("dois", "123456"));
        users.put("tres", new User("tres", "123456"));
        users.put("quatro", new User("quatro", "123456"));
        users.put("cinco", new User("cinco", "123456"));
        users.put("seis", new User("seis", "123456"));
        users.put("sete", new User("sete", "123456"));
        users.put("oito", new User("oito", "123456"));
        users.put("sergio", new User("sergio", "123456"));
        users.put("vitor", new User("vitor", "123456"));
        users.put("marcos", new User("marcos", "123456"));
        users.put("diana", new User("diana", "123456"));
        users.put("zé", new User("zé", "123456"));
        users.put("a", new User("a", "123456"));
        users.put("b", new User("b", "123456"));
        users.put("c", new User("c", "123456"));
        users.put("e", new User("e", "123456"));
        users.put("f", new User("f", "123456"));
        users.put("g", new User("g", "123456"));
        users.put("h", new User("h", "123456"));
        users.put("i", new User("i", "123456"));
        users.put("j", new User("j", "123456"));
        users.put("k", new User("k", "123456"));
        users.put("l", new User("l", "123456"));
        users.put("m", new User("m", "123456"));
        users.put("n", new User("n", "123456"));
        users.put("o", new User("o", "123456"));
        users.put("p", new User("p", "123456"));
        users.put("q", new User("q", "123456"));
        users.put("r", new User("r", "123456"));
        users.put("s", new User("s", "123456"));
        users.put("t", new User("t", "123456"));
        users.put("u", new User("u", "123456"));
        users.put("v", new User("v", "123456"));
        users.put("w", new User("w", "123456"));
        users.put("x", new User("x", "123456"));
        users.put("y", new User("y", "123456"));
        users.put("z", new User("z", "123456"));
        //

        s.startServer();
    }
}
