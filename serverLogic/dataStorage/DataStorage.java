package serverLogic.dataStorage;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;

public class DataStorage {
    private HashMap<String, String> storage;
    private HashMap<String, LocalDateTime> expirations;

    public DataStorage(){
        this.storage = new HashMap<>();
        this.expirations = new HashMap<>();
    }

    public void addToStorage(String key, String value, int milisToExpire){
        // example with expire: *5\r\n$$3\r\nSET\r\n$$3\r\nfoo\r\n$$3\r\nbar\r\n$$2\r\npx\r\n$$5\r\n10000\r\n

        System.out.println("adding to storage key = (" + key + ") value = (" + value + ")"); // LOG

        this.storage.put(key, value);

        if (milisToExpire > 0) {
            System.out.println("adding expiration = (" + milisToExpire + ")"); // LOG

            Duration dur = Duration.ofMillis((long) milisToExpire);
            this.expirations.put(key, LocalDateTime.now().plus(dur));
        }
    }

    private boolean isExpired(String key){
        LocalDateTime expirationDate = this.expirations.get(key);

        System.out.println("Expiration date = " + expirationDate);

        if (expirationDate != null) {
            if (expirationDate.isBefore(LocalDateTime.now())) {
                return true;
            }
        }

        return false;
    }

    public String getFromStorage(String key){
        String retValue = this.storage.get(key);

        if (retValue != null) {
            if (this.isExpired(key)) {
                // redis has two ways of expiring keys
                // passive way - client is trying to acces key that exists in memory but it's time has expired
                // active way - periodically redis removes keys from memory that have already expired
                // this check implements passive key expiration

                System.out.println("value exists but expired"); // LOG
                retValue = null;
            }
        }

        System.out.println("retValue = " + retValue);

        return retValue;
    }
}
