package serverLogic.masterConnection;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

import serverLogic.clientHandler.ClientReplicaSetup;
import serverLogic.clientHandler.CommandProcessor;
import serverLogic.clientHandler.InputParser;
import serverLogic.dataStorage.DataStorage;
import serverLogic.domain.Response;

public class MasterHandler implements Runnable{

    private ExecutorService dataAccessES;
    private DataStorage dataStorage;
    private ConnectionToMaster masterConnection;
    private ClientReplicaSetup clientReplicaSetup;

    public MasterHandler(ExecutorService dataAccessES, DataStorage dataStorage, ConnectionToMaster masterConnection){
        this.dataAccessES = dataAccessES;
        this.dataStorage = dataStorage;
        this.masterConnection = masterConnection;

        this.clientReplicaSetup = new ClientReplicaSetup();
    }

    @Override
    public void run() {

        System.out.println("thread for reading messages from master started " + Thread.currentThread().getName()); // LOG

        DataInputStream ins = this.masterConnection.getDataInputStream();
        byte[] buffer = new byte[2048];

        try {
            while (ins.read(buffer) != -1) {
                System.out.println("Reading message from master"); // LOG
                
                Response r = CommandProcessor.processCommand(new InputParser(buffer), this.dataAccessES, this.dataStorage, this.clientReplicaSetup, null, null, true);

                System.out.println("processed master message, result = " + r.getMessage()); 

                // we are not sending a response
                // beacause it would cause issues with multiple mesasges going through the same stream
            }
        } catch (IOException e) {
            System.out.println("ERROR reading message from masters stream"); // LOG
            e.printStackTrace();
        }

        System.out.println("thread for reading messages from master finished"); // LOG
    }

}
