package serverLogic.commandProcessor;

import serverLogic.clientHandler.InputParser;
import serverLogic.domain.RespDataType;
import serverLogic.domain.Response;

public final class CommandProcessor {
    private CommandProcessor(){}

    public static Response processCommands(InputParser ip){
        Response r = new Response();

        while(true){

            String command = ip.getNextCommand();

            System.out.println("processing command: " + command); // LOG

            if (ip.isError()) {
                r.setMessage(ip.getErrorMessage(), RespDataType.RESP_SIMPLE_ERROR);
                break;
            }

            if (command == null) {
                break;
            }

            command = command.toLowerCase();

            if (command.equals("ping")) {
                CommandProcessor.processPing(r);
            }

            if (command.equals("echo")) {
                CommandProcessor.processEcho(ip, r);
            }

        }

        System.out.println("response to client: " + r.getMessage()); // LOG

        if (r.getMessage() == null) {
            r.setMessage("ERR unknown command", RespDataType.RESP_SIMPLE_ERROR);
        }

        return r;
    }

    private static void processPing(Response r){
        r.setMessage("PONG",RespDataType.RESP_SIMPLE_STRING);
    }

    private static void processEcho(InputParser ip, Response r){
        r.setMessage(ip.getNextCommand(), RespDataType.RESP_BULK_STRING);
    }

}
