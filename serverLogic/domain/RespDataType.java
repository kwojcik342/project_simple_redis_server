package serverLogic.domain;

public enum RespDataType {

    RESP_SIMPLE_STRING('+')
   ,RESP_SIMPLE_ERROR('-')
   ,RESP_INTEGER(':')
   ,RESP_BULK_STRING('$')
   ,RESP_ARRAY('*')
   ,RESP_NULL('_')
   ,RESP_BOOLEAN('#')
   ,RESP_DOUBLE(',')
   ,RESP_BIG_NUMBER('(')
   ,RESP_BULK_ERROR('!')
   ,RESP_VERBATIM_STRING('=')
   ,RESP_MAP('%')
   ,RESP_SET('~')
   ,RESP_PUSH('>');

    public final char firstByte;

    RespDataType(char firstByte){
        this.firstByte = firstByte;
    }
}
