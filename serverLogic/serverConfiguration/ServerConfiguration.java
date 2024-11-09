package serverLogic.serverConfiguration;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Scanner;

public class ServerConfiguration {
    private final String confFilePath = "redis_data/redis.conf";
    private HashMap<String, String> config;

    public ServerConfiguration(String[] cliArgs){
        this.config = new HashMap<>();

        System.out.println("creating config based on file: " + confFilePath);

        this.createDefaultConfig();
        this.loadConfigFromFile();
        this.loadConfigFromCli(cliArgs);
    }

    private void createDefaultConfig(){
        this.config.put(ConfigKeys.CONF_PORT.keyStr, "6379");
        this.config.put(ConfigKeys.CONF_DIR.keyStr, "redis_data");
        this.config.put(ConfigKeys.CONF_DBFILENAME.keyStr, "dump.rdb");
    }

    private void loadConfigFromFile(){
        try (Scanner sc = new Scanner(Paths.get(this.confFilePath))) {

            while (sc.hasNextLine()) {

                String line = sc.nextLine();

                if (!line.isBlank()) {
                    // lines starting with # are comments
                    if (line.charAt(0) != '#') {
                        String[] lineSpl = line.split(" ");
                        if (lineSpl.length == 2) {
                            // configuration values are two strings separated by space
                            ConfigKeys ck = this.isConfigKey(lineSpl[0]);
                            if (ck != null) {
                                System.out.println(lineSpl[0] + " is valid configuration key = " + ck.keyStr); // LOG
                                this.config.put(ck.keyStr, lineSpl[1]);
                            }else{
                                System.out.println(lineSpl[0] + " is not valid configuration key."); // LOG
                            }
                        } else {
                            System.out.println("ERROR reading config value: (" + line + ")");
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("WARNING error reading config file, only default config has been created"); // LOG
            e.printStackTrace();
        }
    }

    private void loadConfigFromCli(String[] cliArgs){
        if (cliArgs.length > 1) {
            System.out.println("Adding cli arguments to config"); // LOG

            for(int i = 0; i < cliArgs.length; i+=2){
                // each configuration should be a key value pair, keys should start with -- (ie: --port 6380)
                ConfigKeys ck = this.isConfigKey(cliArgs[i]);
                if (ck != null) {
                    System.out.println(cliArgs[i] + " is valid configuration key = " + ck.keyStr); // LOG
                    this.config.put(ck.keyStr, cliArgs[i+1].replaceAll("\"", ""));
                }else{
                    System.out.println(cliArgs[i] + " is not valid configuration key."); // LOG
                }
            }
        }else{
            System.out.println("no cli args"); // LOG
        }
    }

    private ConfigKeys isConfigKey(String key){

        String key2 = key.replaceAll("-", "").toLowerCase();

        for(ConfigKeys ck : ConfigKeys.values()){
            if (ck.keyStr.equals(key2)) {
                return ck;
            }
        }

        return null;
    }

    public String getConfigValue(ConfigKeys ck){
        return this.config.get(ck.keyStr);
    }
}
