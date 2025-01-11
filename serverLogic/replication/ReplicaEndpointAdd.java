package serverLogic.replication;

import java.util.concurrent.Callable;

public class ReplicaEndpointAdd implements Callable<Integer>{

    private ReplicationEndpoints rps;
    private ReplicaEndpoint rp;

    public ReplicaEndpointAdd(ReplicationEndpoints rps, ReplicaEndpoint rp){
        this.rps = rps;
        this.rp = rp;
        System.out.println("added new replica endpoint");
    }

    @Override
    public Integer call() throws Exception {
        this.rps.addEndpoint(this.rp);
        return 1;
    }

}
