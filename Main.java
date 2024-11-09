import serverLogic.TcpServer;

public class Main {

    public static void main(String[] args) {
        TcpServer s = new TcpServer(args);
        s.startServer();
    }
}