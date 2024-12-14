package serverLogic.clientHandler;

public class ClientReplicaSetup {
    
    private boolean replHsPing;     // handshake with replica
    private boolean replHsConfPort;
    private boolean replHsConfCapa;
    private boolean replHsPsync;

    public ClientReplicaSetup(){
        this.replHsPing = false;
        this.replHsConfPort = false;
        this.replHsConfCapa = false;
        this.replHsPsync = false;
    }

    public void setReplHsPing(boolean val){
        this.replHsPing = val;
    }

    public void setReplHsConfPort(boolean val){
        this.replHsConfPort = val;
    }

    public void setReplHsConfCapa(boolean val){
        this.replHsConfCapa = val;
    }

    public void setReplHsPsync(boolean val){
        this.replHsPsync = val;
    }

    public boolean isReplHsPing(){
        return this.replHsPing;
    }

    public boolean isReplHsConfPort(){
        return this.replHsConfPort;
    }

    public boolean isReplHsConfCapa(){
        return this.replHsConfCapa;
    }

    public boolean isReplHsPsync(){
        return this.replHsPsync;
    }

    public void resetSetup(){
        this.replHsPing = false;
        this.replHsConfPort = false;
        this.replHsConfCapa = false;
        this.replHsPsync = false;
    }

    public boolean isReplica(){
        return this.replHsPing && this.replHsConfPort && this.replHsConfCapa && this.replHsPsync;
    }
}
