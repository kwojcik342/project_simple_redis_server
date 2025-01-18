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

    public DataInputStream getInputStream(){
        return this.ins;
    }

    public DataOutputStream getOutputStream(){
        return this.outs;
    }

    public void sendMsg(String msg) throws IOException{

        System.out.println("sending message to replica: " + msg);
        
        this.outs.write(msg.getBytes());

        // not reading response because it would cause issues with multiple messages in the same stream
    }
}
