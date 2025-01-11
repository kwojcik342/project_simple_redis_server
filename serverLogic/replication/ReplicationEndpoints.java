package serverLogic.replication;

import java.util.Iterator;
import java.util.LinkedList;

public class ReplicationEndpoints {
    private LinkedList<ReplicaEndpoint> res;

    public ReplicationEndpoints(){
        this.res = new LinkedList<>();
    }

    public void addEndpoint(ReplicaEndpoint re){
        this.res.add(new ReplicaEndpoint(re.getInputStream(), re.getOutputStream()));
    }

    public void replicateMessage(String message){
        System.out.println("replicating message " + message);
        if (this.res.size() > 0) {

            System.out.println("replica endpoints exist");

            Iterator<ReplicaEndpoint> itRE = this.res.iterator();
            
            while(itRE.hasNext()){
                try {
                    ReplicaEndpoint re = itRE.next();
                    re.sendMsg(message);
                } catch (Exception e) {
                    System.out.println("ERROR! replication error - removing replica endpoint");
                    e.printStackTrace();
                    itRE.remove();
                }
            }
        }
    }
}
