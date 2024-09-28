package serverLogic.clientHandler;

import serverLogic.domain.RespDataType;

public class InputParser {

    // parses array of bytes according to resp protocol

    private static final int separatorOffset = 4;

    private byte[] inputCommand;
    private int offset;
    private int argumentsCount;
    private int processedArguments;
    private String errorMessage;
    private boolean isError;

    public InputParser(byte[] inputBuffer){
        this.inputCommand = inputBuffer;
        this.offset = 0;
        this.argumentsCount = 0;
        this.processedArguments = 0;
        this.errorMessage = null;
        this.isError = false;

        this.initParser();
    }

    private void initParser(){

        // command should be a RESP array of bulk strings
        // each array is a single command
        // each bulk string is argument of command
        if ((char) this.inputCommand[0] == RespDataType.RESP_ARRAY.firstByte) {

            try {
                this.argumentsCount = Integer.valueOf(this.getNextChunk().substring(1));
            } catch (NumberFormatException e) {
                this.setError("ERR invalid value for number of arguments in array");
            }
            
            //System.out.println(this.argumentsCount); // LOG

        } else {
            this.setError("ERR command not a RESP array");
            System.out.println(this.errorMessage);
        }
    }

    private String getNextChunk(){
        // returns next chunk from byte array in form of String
        // chunk means bytes from this.offset to next separator

        int chunkLength = 0;

        for(int i = this.offset; i < this.inputCommand.length; i++){
            char c = (char) this.inputCommand[i];

            if (c == '\\') {
                break;
            }

            chunkLength++;
        }

        String nextChunk = new String(inputCommand, this.offset, chunkLength);

        this.offset += (chunkLength + InputParser.separatorOffset);

        return nextChunk;
    }

    public String getNextArgument(){

        // returns null if there are no more arguments to process or on error
        String command = null;
        
        if (this.processedArguments < this.argumentsCount) {
            String bulkStrHeader = this.getNextChunk();
            //System.out.println("getNextArgument bulkStrHeader = " + bulkStrHeader); // LOG

            if (bulkStrHeader.charAt(0) == RespDataType.RESP_BULK_STRING.firstByte) {
                int commandLength = 0;

                try {
                    commandLength = Integer.valueOf(bulkStrHeader.substring(1));
                } catch (NumberFormatException e) {
                    this.setError("ERR next argument invalid value for bulk string length");
                    System.out.println(this.errorMessage); // LOG
                }

                if (commandLength > 0) {
                    command = this.getNextChunk();
                    //System.out.println("getNextArgument command = " + command); // LOG

                    this.processedArguments++;
                }
            } else {
                this.setError("ERR next command not a bulk string");
            }
        }

        return command;
    }

    private void setError(String errMessage){
        this.errorMessage = errMessage;
        this.isError = true;
    }

    public boolean isError(){
        return this.isError;
    }

    public String getErrorMessage(){
        return this.errorMessage;
    }
}
