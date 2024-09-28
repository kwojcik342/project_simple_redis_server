package serverLogic.dataStorage.dataAccessCommands;

import java.util.concurrent.Callable;

import serverLogic.dataStorage.DataStorage;

public class DataAccessGet implements Callable<String>{

    private DataStorage dataStorage;
    private String key;

    public DataAccessGet(DataStorage dataStorage, String key){
        this.dataStorage = dataStorage;
        this.key = key;
    }

    @Override
    public String call() throws Exception {
        return this.dataStorage.getFromStorage(this.key);
    }

}
