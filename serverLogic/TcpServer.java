package serverLogic;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import serverLogic.clientHandler.ClientHandler;
import serverLogic.dataStorage.DataStorage;
import serverLogic.dataStorage.DumpReader;
import serverLogic.masterConnection.ConnectionToMaster;
import serverLogic.masterConnection.MasterConnectionSetup;
import serverLogic.serverConfiguration.ConfigKeys;
import serverLogic.serverConfiguration.ServerConfiguration;

public class TcpServer {
    private ServerConfiguration config;
    private ServerSocket server;
    private ExecutorService connectionES;
    private ExecutorService dataAccessES;
    private DataStorage dataStorage;
    private ConnectionToMaster masterConnection;


    public TcpServer(String[] cliArgs){
        this.config = new ServerConfiguration(cliArgs);
    }

    public void startServer(){

        // executor service for handling multiple client connections at the same time
        this.connectionES = Executors.newCachedThreadPool();

        // according to documentation redis is single threaded which means clients can't access the same memory concurently
        // we have separate executor service which executes client's requests sequentialy on single thread
        this.dataAccessES = Executors.newSingleThreadExecutor();

        // check if instance is supposed to be slave
        this.masterConnection = MasterConnectionSetup.getConnectionToMaster(this.config);

        if (this.masterConnection != null) {
            // TODO: somehow get data for this.dataStorage from this.masterConnection
            this.dataStorage = null;
        }else{
            // if not slave try getting initial data from dump file
            this.dataStorage = DumpReader.readRdbData(this.config.getConfigValue(ConfigKeys.CONF_DIR) + "\\" + this.config.getConfigValue(ConfigKeys.CONF_DBFILENAME));
        }

        if (this.dataStorage == null) {
            this.dataStorage = new DataStorage();
        }

        try {
            this.server = new ServerSocket(Integer.valueOf(this.config.getConfigValue(ConfigKeys.CONF_PORT)));

            while (true) {
                Socket client = this.server.accept();
                connectionES.submit(new ClientHandler(client, dataAccessES, this.dataStorage));
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
