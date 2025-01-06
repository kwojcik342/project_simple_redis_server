package serverLogic.replication;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ReplicaEndpoint {
    private DataInputStream ins;
    private DataOutputStream outs;

    public ReplicaEndpoint(DataInputStream ins, DataOutputStream outs){
        this.ins = ins;
        this.outs = outs;
    }

    public void sendMsg(String msg) throws IOException{

        System.out.println("sending message to replica: " + msg);
        
        this.outs.write(msg.getBytes());

        byte[] buffer = new byte[2048];
        this.ins.read(buffer);
        String responseFromReplica = new String(buffer).trim();
        System.out.println("response from replica = " + responseFromReplica);
    }
}
