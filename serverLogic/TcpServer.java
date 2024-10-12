package serverLogic;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import serverLogic.clientHandler.ClientHandler;
import serverLogic.dataStorage.DataStorage;
import serverLogic.serverConfiguration.ServerConfiguration;

public class TcpServer {
    private ServerConfiguration config;
    private ServerSocket server;
    private ExecutorService connectionES;
    private ExecutorService dataAccessES;
    private DataStorage dataStorage;


    public TcpServer(String confFilePath){
        this.config = new ServerConfiguration(confFilePath);
    }

    public void startServer(){

        // executor service for handling multiple client connections at the same time
        this.connectionES = Executors.newCachedThreadPool();

        // according to documentation redis is single threaded which means clients can't access the same memory concurently
        // we have separate executor service which executes client's requests sequentialy on single thread
        this.dataAccessES = Executors.newSingleThreadExecutor();

        dataStorage = new DataStorage();

        try {
            this.server = new ServerSocket(Integer.valueOf(this.config.getConfigValue("port")));

            while (true) {
                Socket client = this.server.accept();
                connectionES.submit(new ClientHandler(client, dataAccessES, dataStorage));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally{

            this.connectionES.shutdown();
            try {
                this.connectionES.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
            }

            this.dataAccessES.shutdown();
            try {
                this.dataAccessES.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
            }

            try {
                this.server.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
