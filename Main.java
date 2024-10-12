import serverLogic.TcpServer;

public class Main {

    public static void main(String[] args) {


        String confFilePath = null;

        if (args.length > 0) {
            confFilePath = args[0];
        }

        TcpServer s = new TcpServer(confFilePath);
        s.startServer();
    }
}