package serverLogic.dataStorage.dataAccessCommands;

import java.util.List;
import java.util.concurrent.Callable;

import serverLogic.dataStorage.DataStorage;

public class DataAccessKeys implements Callable<List<String>>{

    private DataStorage ds;
    private String pattern;

    public DataAccessKeys(DataStorage ds, String pattern){
        this.ds = ds;
        this.pattern = pattern;
    }

    @Override
    public List<String> call() throws Exception {
        return this.ds.getKeys(this.pattern);
    }

}
