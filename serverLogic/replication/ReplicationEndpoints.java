package serverLogic.replication;

import java.util.Iterator;
import java.util.LinkedList;

public class ReplicationEndpoints {
    private LinkedList<ReplicaEndpoint> res;

    public ReplicationEndpoints(){
        this.res = new LinkedList<>();
    }

    public void replicateMessage(String message){
        if (this.res.size() > 0) {

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
