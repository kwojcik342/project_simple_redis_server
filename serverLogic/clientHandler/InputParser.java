package serverLogic.clientHandler;

import serverLogic.domain.RespDataType;

public class InputParser {

    // general note: if any error occurs we won't be processing further commands

    private static final int separatorOffset = 4;

    private byte[] inputCommands;
    private int offset;
    private int commandsCount;
    private int processedCommands;
    private String errorMessage;
    private boolean isError;

    public InputParser(byte[] inputBuffer){
        this.inputCommands = inputBuffer;
        this.offset = 0;
        this.commandsCount = 0;
        this.processedCommands = 0;
        this.errorMessage = null;
        this.isError = false;

        this.initParser();
    }

    private void initParser(){

        // command should be a RESP array of bulk strings
        if ((char) this.inputCommands[0] == RespDataType.RESP_ARRAY.firstByte) {

            try {
                this.commandsCount = Integer.valueOf(this.getNextChunk().substring(1));
            } catch (NumberFormatException e) {
                this.setError("ERR invalid value for number of commands in array");
            }
            
            //System.out.println(this.commandsCount); // LOG

        } else {
            this.setError("ERR command not a RESP array");
            System.out.println(this.errorMessage);
        }
    }

    private String getNextChunk(){
        // returns next chunk from byte array in form of String
        // chunk means bytes from this.offset to next separator

        int chunkLength = 0;

        for(int i = this.offset; i < this.inputCommands.length; i++){
            char c = (char) this.inputCommands[i];

            if (c == '\\') {
                break;
            }

            chunkLength++;
        }

        String nextChunk = new String(inputCommands, this.offset, chunkLength);

        this.offset += (chunkLength + InputParser.separatorOffset);

        return nextChunk;
    }

    public String getNextCommand(){

        // returns null if there are no more commands to process or on error
        String command = null;
        
        if (this.processedCommands < this.commandsCount) {
            String bulkStrHeader = this.getNextChunk();
            //System.out.println("getNextCommand bulkStrHeader = " + bulkStrHeader); // LOG

            if (bulkStrHeader.charAt(0) == RespDataType.RESP_BULK_STRING.firstByte) {
                int commandLength = 0;

                try {
                    commandLength = Integer.valueOf(bulkStrHeader.substring(1));
                } catch (NumberFormatException e) {
                    this.setError("ERR next command invalid value for bulk string length");
                    System.out.println(this.errorMessage); // LOG
                }

                if (commandLength > 0) {
                    command = this.getNextChunk();
                    //System.out.println("getNextCommand command = " + command); // LOG

                    this.processedCommands++;
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
