package serverLogic;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import serverLogic.clientHandler.ClientHandler;

public class TcpServer {
    private ServerSocket server;

    public void startServer(int port){

        ExecutorService es = Executors.newCachedThreadPool();

        try {
            this.server = new ServerSocket(port);

            while (true) {
                Socket client = this.server.accept();
                es.submit(new ClientHandler(client));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
