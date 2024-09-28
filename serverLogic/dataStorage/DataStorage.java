package serverLogic.dataStorage;

import java.util.HashMap;

public class DataStorage {
    private HashMap<String, StorageValue> storage;

    public DataStorage(){
        this.storage = new HashMap<>();
    }

    public void addToStorage(String key, String value, int milisToExpire){
        System.out.println("adding to storage key = (" + key + ") value = (" + value + ")"); // LOG

        this.storage.put(key, new StorageValue(value, milisToExpire));
    }

    public String getFromStorage(String key){
        StorageValue value = this.storage.get(key);
        String retValue = null;

        if (value != null) {
            if (!value.isExpired()) {
                // redis has two ways of expiring keys
                // passive way - client is trying to acces key that exists in memory but it's time has expired
                // active way - periodically redis removes keys from memory that have already expired
                // this check implements passive key expiration
                retValue = value.getValue();
            }
        }

        System.out.println("retValue = " + retValue);

        return retValue;
    }
}
