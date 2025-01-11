package serverLogic.replication;

import java.util.concurrent.Callable;

public class ReplicateMessage implements Callable<Integer>{

    private String message;
    private ReplicationEndpoints rps;

    public ReplicateMessage(String message, ReplicationEndpoints rps){
        this.message = message;
        this.rps = rps;
    }

    @Override
    public Integer call() throws Exception {
        this.rps.replicateMessage(message);
        return 1;
    }

}
