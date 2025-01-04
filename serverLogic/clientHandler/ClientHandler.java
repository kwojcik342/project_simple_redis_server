package serverLogic.clientHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import serverLogic.dataStorage.DataStorage;
import serverLogic.domain.Response;
import serverLogic.masterConnection.ConnectionToMaster;

public class ClientHandler implements Runnable{

    private Socket client;
    private ExecutorService dataAccessES;
    private DataStorage dataStorage;
    private ClientReplicaSetup clientReplicaSetup;
    private ConnectionToMaster masterConnection;

    public ClientHandler(Socket client, ExecutorService dataAccessES, DataStorage dataStorage, ConnectionToMaster masterConnection){
        this.client = client;
        this.dataAccessES = dataAccessES;
        this.dataStorage = dataStorage;
        this.masterConnection = masterConnection;

        this.clientReplicaSetup = new ClientReplicaSetup();
    }

    @Override
    public void run() {

        try {
            DataInputStream ins = new DataInputStream(this.client.getInputStream());
            DataOutputStream outs = new DataOutputStream(this.client.getOutputStream());

            byte[] buffer = new byte[2048];

            while (ins.read(buffer) != -1) {

                Response r = CommandProcessor.processCommand(new InputParser(buffer), this.dataAccessES, this.dataStorage, this.clientReplicaSetup, this.masterConnection);

                //outs.writeBytes(r.getMessage());
                outs.write(r.getMessage().getBytes());

                if (r.isFinal()) {
                    break;
                }
            }

            ins.close();
            outs.close();
            this.client.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
   
}
