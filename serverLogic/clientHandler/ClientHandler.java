package serverLogic.clientHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import serverLogic.dataStorage.DataStorage;
import serverLogic.domain.RespDataType;
import serverLogic.domain.Response;
import serverLogic.masterConnection.ConnectionToMaster;
import serverLogic.replication.ReplicaEndpoint;
import serverLogic.replication.ReplicaEndpointAdd;
import serverLogic.replication.ReplicationEndpoints;

public class ClientHandler implements Runnable{

    private Socket client;
    private ExecutorService dataAccessES;
    private DataStorage dataStorage;
    private ClientReplicaSetup clientReplicaSetup;
    private ConnectionToMaster masterConnection;
    private ReplicationEndpoints replicationEndpoints;

    public ClientHandler(Socket client, ExecutorService dataAccessES, DataStorage dataStorage, ConnectionToMaster masterConnection, ReplicationEndpoints replicationEndpoints){
        this.client = client;
        this.dataAccessES = dataAccessES;
        this.dataStorage = dataStorage;
        this.masterConnection = masterConnection;
        this.replicationEndpoints = replicationEndpoints;

        this.clientReplicaSetup = new ClientReplicaSetup();
    }

    private int addReplicaEndpoint(DataInputStream ins, DataOutputStream outs){
        int res = 1;
        Future<Integer> addReFuture = this.dataAccessES.submit(new ReplicaEndpointAdd(this.replicationEndpoints, new ReplicaEndpoint(ins, outs)));

        try {
            res = addReFuture.get();
        } catch (Exception e) {
            e.printStackTrace();
            res = -1;
        }

        return res;
    }

    @Override
    public void run() {

        try {
            DataInputStream ins = new DataInputStream(this.client.getInputStream());
            DataOutputStream outs = new DataOutputStream(this.client.getOutputStream());

            byte[] buffer = new byte[2048];

            while (ins.read(buffer) != -1) {

                Response r = CommandProcessor.processCommand(new InputParser(buffer), this.dataAccessES, this.dataStorage, this.clientReplicaSetup, this.masterConnection, this.replicationEndpoints, false);

                if (this.clientReplicaSetup.isHandshakeEstablished() && !this.clientReplicaSetup.isReplica()) {
                    if (this.addReplicaEndpoint(ins, outs) != 1) {
                        r = new Response();
                        r.setMessage("ERR adding endpoint to list failed", RespDataType.RESP_SIMPLE_ERROR);
                    } else {
                        this.clientReplicaSetup.setIsReplica(true);
                    }
                }

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
