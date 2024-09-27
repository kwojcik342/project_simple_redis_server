package serverLogic.clientHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import serverLogic.commandProcessor.CommandProcessor;
import serverLogic.domain.Response;

public class ClientHandler implements Runnable{

    private Socket client;

    public ClientHandler(Socket client){
        this.client = client;
    }

    @Override
    public void run() {

        try {
            DataInputStream ins = new DataInputStream(this.client.getInputStream());
            DataOutputStream outs = new DataOutputStream(this.client.getOutputStream());

            byte[] buffer = new byte[2048];

            while (ins.read(buffer) != -1) {

                Response r = CommandProcessor.processCommands(new InputParser(buffer));

                outs.writeBytes(r.getMessage());

                if (r.isFinal()) {
                    break;
                }
            }

            ins.close();
            outs.close();
            this.client.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
   
}
