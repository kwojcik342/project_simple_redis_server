package serverLogic.dataStorage;

import java.time.Duration;
import java.time.LocalDateTime;

public class StorageValue {
    private String value;
    private LocalDateTime expireDateTime;

    public StorageValue(String value, int milisToExpire){
        this.value = value;

        if (milisToExpire > 0) {
            Duration dur = Duration.ofMillis((long) milisToExpire);
            this.expireDateTime = LocalDateTime.now().plus(dur);
        } else {
            this.expireDateTime = null;
        }
        
    }

    public String getValue(){
        return this.value;
    }

    public boolean isExpired(){
        if (this.expireDateTime == null) {
            return false;
        }

        if (this.expireDateTime.isAfter(LocalDateTime.now())) {
            return true;
        }

        return false;
    }
}
