package serverLogic.masterConnection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import serverLogic.serverConfiguration.ConfigKeys;
import serverLogic.serverConfiguration.ServerConfiguration;

public final class MasterConnectionSetup {

    private MasterConnectionSetup(){}

    public static ConnectionToMaster getConnectionToMaster(ServerConfiguration config){

        // if this app instace is configured to run as slave try setting up connection to master

        String replicaOf = config.getConfigValue(ConfigKeys.CONF_REPLICAOF);

        if (replicaOf == null) {
            System.out.println("No configuration for connection to master.");
        }else{
            ConnectionToMaster c2m = MasterConnectionSetup.initSocket(replicaOf, config);

            if (c2m != null) {
                if (c2m.handshake()) {
                    System.out.println("Handshake process successful - connection to master established");
                    return c2m;
                }
            }
        }

        return null;
    }

    private static ConnectionToMaster initSocket(String replicaOf, ServerConfiguration config){
        String[] replicaOfSplit = replicaOf.split(" ");

        if (replicaOfSplit.length != 2) {
            System.out.println("Incorrect connection data for master " + replicaOf);
        }else{
            int port = -1;

            try {
                port = Integer.valueOf(replicaOfSplit[1]);
            } catch (NumberFormatException e) {
                port = -1;
                System.out.println("Parsing port to int failed.");
            }

            if (port > 0) {
                try {
                    Socket masterSocket = new Socket(replicaOfSplit[0], port);
                    DataInputStream ins = new DataInputStream(masterSocket.getInputStream());
                    DataOutputStream outs = new DataOutputStream(masterSocket.getOutputStream());

                    return new ConnectionToMaster(masterSocket, ins, outs, config);

                } catch (Exception e) {
                    System.out.println("ERROR on connection to master setup.");
                    e.printStackTrace();
                    System.out.println("");
                }
            }else{
                System.out.println("Incorrect value for port");
            }
        }

        return null;
    }
}
