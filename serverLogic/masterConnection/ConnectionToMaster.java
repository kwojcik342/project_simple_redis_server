package serverLogic.masterConnection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import serverLogic.domain.RespDataType;
import serverLogic.domain.Response;

public class ConnectionToMaster {

    private Socket conToMaster;
    private DataInputStream ins;
    private DataOutputStream outs;

    public ConnectionToMaster(Socket conToMaster, DataInputStream ins, DataOutputStream outs){
        this.conToMaster = conToMaster;
        this.ins = ins;
        this.outs = outs;
    }

    public void closeConnection(){
        try {
            this.ins.close();
        } catch (IOException e) {
            System.out.println("error closing input stream from master");
            e.printStackTrace();
            System.out.println("");
        }

        try {
            this.outs.close();
        } catch (IOException e) {
            System.out.println("error closing output stream to master");
            e.printStackTrace();
            System.out.println("");
        }

        try {
            this.conToMaster.close();
        } catch (IOException e) {
            System.out.println("error closing master socket");
            e.printStackTrace();
            System.out.println("");
        }
        
    }

    public boolean handshake(){

        if (this.handshakePing()) {
            return true;
        }else{
            return false;
        }
    }

    private boolean handshakePing(){
        final String msg = "PING";

        System.out.println("Trying handshake ping");

        String response = this.sendMsg(this.parseMsgToRespProtocol(msg), true);

        if (response.contains("OK")) {
            System.out.println("Handshake ping succesfull");
            return true;
        }else{
            System.out.println("Handshake ping failed");
            return false;
        }
    }

    public String parseMsgToRespProtocol(String msg){
        Response r = new Response();
        r.setMessage(msg, RespDataType.RESP_BULK_STRING);
        r.setRespAsArray(true);
        return r.getMessage();
    }

    public String sendMsg(String msg, boolean getResponse){
        String responseFromServer = "OK";

        System.out.println("sending message to master: " + msg);
        
        try {
            this.outs.writeBytes(msg);

            if (getResponse) {
                byte[] buffer = new byte[2048];
                this.ins.read(buffer);
                responseFromServer = new String(buffer).trim();
                System.out.println("response from server = " + responseFromServer);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return "ERR";
        }

        return responseFromServer;
    }
}
