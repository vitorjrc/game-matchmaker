package matchmaker;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    private ServerSocket servsocket;
    private int porto;
    private static ConcurrentHashMap<String, User> users = new ConcurrentHashMap<String, User>();

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

                ServerWorker sw = new ServerWorker(socket, workerCounter++, users);
                new Thread(sw).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Server s = new Server(12345);

        // Testar users
        User user1 = new User("um", "123456");
        User user2 = new User("dois", "123456");
        User user3 = new User("tres", "123456");
        User user4 = new User("quatro", "123456");
        User user5 = new User("cinco", "123456");
        User user6 = new User("seis", "123456");
        User user7 = new User("sete", "123456");
        User user8 = new User("oito", "123456");
        User user9 = new User("sergio", "123456");
        User user10 = new User("vitor", "123456");
        User user11 = new User("marcos", "123456");
        User user12 = new User("diana", "123456");
        users.put("um", user1);
        users.put("dois", user2);
        users.put("tres", user3);
        users.put("quatro", user4);
        users.put("cinco", user5);
        users.put("seis", user6);
        users.put("sete", user7);
        users.put("oito", user8);
        users.put("sergio", user9);
        users.put("vitor", user10);
        users.put("marcos", user11);
        users.put("diana", user12);
        //

        s.startServer();
    }
}
