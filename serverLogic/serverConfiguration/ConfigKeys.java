package serverLogic.serverConfiguration;

public enum ConfigKeys {
    
    CONF_PORT("port")
   ,CONF_DIR("dir")
   ,CONF_DBFILENAME("dbfilename");

   public final String keyStr;

   ConfigKeys(String keyStr){
        this.keyStr = keyStr;
   }
}
