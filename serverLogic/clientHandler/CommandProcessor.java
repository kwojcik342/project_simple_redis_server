package serverLogic.clientHandler;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import serverLogic.dataStorage.DataStorage;
import serverLogic.dataStorage.dataAccessCommands.DataAccessGet;
import serverLogic.dataStorage.dataAccessCommands.DataAccessKeys;
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
                // example: *1\r\n$$4\r\nPING\r\n

                CommandProcessor.processPing(r);
            }

            if (command.equals("echo")) {
                // example: *2\r\n$4\r\nECHO\r\n$3\r\nhey\r\n
                CommandProcessor.processEcho(ip, r);
            }

            if (command.equals("set")) {
                // example with expire: *5\r\n$$3\r\nSET\r\n$$3\r\nfoo\r\n$$3\r\nbar\r\n$$2\r\npx\r\n$$5\r\n20000\r\n
                // example no expiration: *3\r\n$$3\r\nSET\r\n$$3\r\nhey\r\n$$3\r\nbye\r\n

                CommandProcessor.processSet(ip, dataAccessES, dataStorage, r);
            }

            if (command.equals("get")) {
                // example: *2\r\n$$3\r\nGET\r\n$$3\r\nfoo\r\n
                CommandProcessor.processGet(ip, dataAccessES, dataStorage, r);
            }

            if (command.equals("keys")) {
                // example: *2\r\n$$4\r\nKEYS\r\n$$1\r\n*\r\n
                CommandProcessor.processKeys(ip, dataAccessES, dataStorage, r);
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

    private static void processKeys(InputParser ip, ExecutorService dataAccessES, DataStorage dataStorage, Response r){

        String pattern = ip.getNextArgument();

        System.out.println("Processing keys for pattern = " + pattern);

        if (pattern != null) {
            Future<List<String>> commandFuture = dataAccessES.submit(new DataAccessKeys(dataStorage, pattern));

            try {
                r.setMessage(commandFuture.get());
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
            r.setMessage("ERR empty pattern value", RespDataType.RESP_SIMPLE_ERROR);
        }
    }

}
