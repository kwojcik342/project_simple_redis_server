package serverLogic.commandProcessor;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import serverLogic.clientHandler.InputParser;
import serverLogic.dataStorage.DataStorage;
import serverLogic.dataStorage.dataAccessCommands.DataAccessGet;
import serverLogic.dataStorage.dataAccessCommands.DataAccessSet;
import serverLogic.domain.RespDataType;
import serverLogic.domain.Response;

public final class CommandProcessor {
    // processes commands received from client

    private CommandProcessor(){}

    public static Response processCommand(InputParser ip, ExecutorService dataAccessES, DataStorage dataStorage){
        Response r = new Response();

        while(true){

            String command = ip.getNextArgument();

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

            if (command.equals("set")) {
                CommandProcessor.processSet(ip, dataAccessES, dataStorage, r);
            }

            if (command.equals("get")) {
                CommandProcessor.processGet(ip, dataAccessES, dataStorage, r);
            }

        }

        System.out.println("response to client (" + Thread.currentThread().getName() + "): " + r.getMessage()); // LOG

        if (r.getMessage() == null) {
            r.setMessage("ERR unknown command", RespDataType.RESP_SIMPLE_ERROR);
        }

        return r;
    }

    private static void processPing(Response r){
        r.setMessage("PONG",RespDataType.RESP_SIMPLE_STRING);
    }

    private static void processEcho(InputParser ip, Response r){
        r.setMessage(ip.getNextArgument(), RespDataType.RESP_BULK_STRING);
    }

    private static void processSet(InputParser ip, ExecutorService dataAccessES, DataStorage dataStorage, Response r){
        String key = ip.getNextArgument();
        String value = ip.getNextArgument();

        if (key == null) {
            r.setMessage("ERR empty key", RespDataType.RESP_SIMPLE_ERROR);
        }

        if (value == null) {
            r.setMessage("ERR empty value", RespDataType.RESP_SIMPLE_ERROR);
        }

        int milisToExpire = 0;

        String nextArgument = null;

        while ((nextArgument = ip.getNextArgument()) != null) {
            if (nextArgument.equals("px")) {
                try {
                    String milisToExpStr = ip.getNextArgument();
                    System.out.println("milisToExpStr = (" + milisToExpStr + ") length = " + milisToExpStr.length());
                    milisToExpire = Integer.valueOf(milisToExpStr);
                } catch (NumberFormatException e) {
                    r.setMessage("ERR invalid value for expire time", RespDataType.RESP_SIMPLE_ERROR);
                }
            }
        }

        if (r.getMessage() == null) {
            Future<String> commandFuture = dataAccessES.submit(new DataAccessSet(dataStorage, key, value, milisToExpire));

            try {
                r.setMessage(commandFuture.get(), RespDataType.RESP_SIMPLE_STRING);
            } catch (CancellationException ce) {
                ce.printStackTrace();
                r.setMessage("ERR task interrupted", RespDataType.RESP_SIMPLE_ERROR);
            } catch (InterruptedException ie){
                ie.printStackTrace();
                r.setMessage("ERR task interrupted", RespDataType.RESP_SIMPLE_ERROR);
            } catch (ExecutionException ee){
                ee.printStackTrace();
                r.setMessage("ERR unhandled exception during command execution", RespDataType.RESP_SIMPLE_ERROR);
            }
        }
    }

    private static void processGet(InputParser ip, ExecutorService dataAccessES, DataStorage dataStorage, Response r){
        String key = ip.getNextArgument();

        System.out.println("Processing GET for key = " + key);

        if (key != null) {
            Future<String> commandFuture = dataAccessES.submit(new DataAccessGet(dataStorage, key));

            try {
                r.setMessage(commandFuture.get(), RespDataType.RESP_BULK_STRING);
            } catch (CancellationException ce) {
                ce.printStackTrace();
                r.setMessage("ERR task interrupted", RespDataType.RESP_SIMPLE_ERROR);
            } catch (InterruptedException ie){
                ie.printStackTrace();
                r.setMessage("ERR task interrupted", RespDataType.RESP_SIMPLE_ERROR);
            } catch (ExecutionException ee){
                ee.printStackTrace();
                r.setMessage("ERR unhandled exception during command execution", RespDataType.RESP_SIMPLE_ERROR);
            }
        } else {
            r.setMessage("ERR empty key value", RespDataType.RESP_SIMPLE_ERROR);
        }
    }

}
