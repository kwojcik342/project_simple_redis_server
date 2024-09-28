package serverLogic.dataStorage.dataAccessCommands;

import java.util.concurrent.Callable;

import serverLogic.dataStorage.DataStorage;

public class DataAccessSet implements Callable<String>{

    private DataStorage dataStorage;
    private String key;
    private String value;
    private int milisToExpire;

    public DataAccessSet(DataStorage dataStorage, String key, String value, int milisToExpire){
        this.dataStorage = dataStorage;
        this.key = key;
        this.value = value;
        this.milisToExpire = milisToExpire;
    }

    @Override
    public String call() throws Exception {
        dataStorage.addToStorage(this.key, this.value, this.milisToExpire);
        return "OK";
    }

    

}
