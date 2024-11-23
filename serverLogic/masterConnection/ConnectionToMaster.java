package serverLogic.masterConnection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import serverLogic.domain.RespDataType;
import serverLogic.domain.Response;
import serverLogic.serverConfiguration.ConfigKeys;
import serverLogic.serverConfiguration.ServerConfiguration;

public class ConnectionToMaster {

    private Socket conToMaster;
    private DataInputStream ins;
    private DataOutputStream outs;
    private ServerConfiguration config;

    public ConnectionToMaster(Socket conToMaster, DataInputStream ins, DataOutputStream outs, ServerConfiguration config){
        this.conToMaster = conToMaster;
        this.ins = ins;
        this.outs = outs;
        this.config = config;
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
            if (this.handshakeReplconf()) {
                if (this.handshakePsync()) {
                    return true;
                }
            }
        }
        
        return false;
    }

    private boolean handshakePing(){

        System.out.println("Trying handshake ping"); // LOG

        Response msg = new Response();
        msg.setMessage("PING", RespDataType.RESP_BULK_STRING);
        msg.setRespAsArray(true);

        String response = this.sendMsg(msg.getMessage(), true);

        if (response.contains("PONG")) {
            System.out.println("Handshake ping successfull"); // LOG
            return true;
        }else{
            System.out.println("Handshake ping failed"); // LOG
            return false;
        }
    }

    private boolean handshakeReplconf(){

        System.out.println("Trying handshake replconf"); // LOG

        Response msg = new Response();
        msg.setMessage("REPLCONF", RespDataType.RESP_BULK_STRING);
        msg.setMessage("listening-port", RespDataType.RESP_BULK_STRING);
        msg.setMessage(this.config.getConfigValue(ConfigKeys.CONF_PORT), RespDataType.RESP_BULK_STRING);

        String response = this.sendMsg(msg.getMessage(), true);

        if (response.contains("OK")) {

            System.out.println("REPLCONF listening-port successful"); // LOG

            // configuration of replication capabilities
            // doesn't do anything in this project but let's just make whole handshake thing according to documentation
            msg = new Response();
            msg.setMessage("REPLCONF", RespDataType.RESP_BULK_STRING);
            msg.setMessage("capa", RespDataType.RESP_BULK_STRING);
            msg.setMessage("psync2", RespDataType.RESP_BULK_STRING);

            response = this.sendMsg(msg.getMessage(), true);

            if (response.contains("OK")) {
                System.out.println("REPLCONF capa successful"); // LOG
                return true;
            }
        }

        return false;
    }

    private boolean handshakePsync(){

        System.out.println("Trying handshake psync"); // LOG

        Response msg = new Response();
        msg.setMessage("PSYNC", RespDataType.RESP_BULK_STRING);
        msg.setMessage("?", RespDataType.RESP_BULK_STRING);
        msg.setMessage("-1", RespDataType.RESP_BULK_STRING);

        String response = this.sendMsg(msg.getMessage(), true);

        if (response.charAt(0) == '-') {
            // char - means response was encoded as simple error
            return false;
        }

        return true;
    }

    public String sendMsg(String msg, boolean getResponse){
        String responseFromServer = "OK";

        System.out.println("sending message to master: " + msg);
        
        try {
            //this.outs.writeBytes(msg);
            //this.outs.flush();
            this.outs.write(msg.getBytes());

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
