package serverLogic.serverConfiguration;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Scanner;

public class ServerConfiguration {
    private String confFilePath;
    private HashMap<String, String> config;

    public ServerConfiguration(String confFilePath){
        this.confFilePath = confFilePath;
        this.config = new HashMap<>();

        this.createDefaultConfig();

        if (this.confFilePath != null) {
            this.loadConfigFromFile();
        } 
    }

    private void createDefaultConfig(){
        this.config.put("port", "6379");
        this.config.put("dir", "redis_data");
        this.config.put("dbfilename", "rdbfile_" + LocalDateTime.now().toString());
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
                            this.config.put(lineSpl[0], lineSpl[1]);
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

    public String getConfigValue(String configParam){
        return this.config.get(configParam);
    }
}
