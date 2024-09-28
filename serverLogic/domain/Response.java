package serverLogic.domain;

public class Response {
    private boolean isFinal; // if true it means connection with client will be closed after we send this response
    private StringBuilder responseMessage;
    private int responseMsgCount;

    private final String separator = "\\r\\n";

    public Response(){
        this.isFinal = false;
        this.responseMessage = new StringBuilder();
        this.responseMsgCount = 0;
    }

    public void setIsFinal(boolean isFinal){
        this.isFinal = isFinal;
    }

    public void setMessage(String message, RespDataType rdt){
        this.responseMessage.append(rdt.firstByte);

        if (rdt == RespDataType.RESP_BULK_STRING) {
            if (message == null || message.isBlank()) {
                // empty bulk string
                this.responseMessage.append("-1");
                this.responseMessage.append(this.separator);
                this.responseMsgCount++;
                return;
            }

            this.responseMessage.append(message.length());
            this.responseMessage.append(this.separator);
        }

        this.responseMessage.append(message);
        this.responseMessage.append(separator);
        this.responseMsgCount++;
    }

    public boolean isFinal(){
        return this.isFinal;
    }
    
    public String getMessage(){
        if (this.responseMsgCount > 0) {
            if (this.responseMsgCount > 1) {
                // multiple messages require encoding response as resp array
                this.responseMessage.insert(0, this.separator);
                this.responseMessage.insert(0, responseMsgCount);
                this.responseMessage.insert(0, RespDataType.RESP_ARRAY.firstByte);
            }
            return this.responseMessage.toString();
        }else {
            return null;
        }
    }
}