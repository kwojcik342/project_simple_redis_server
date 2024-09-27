import serverLogic.TcpServer;

public class Main {

    public static void main(String[] args) {
        TcpServer s = new TcpServer();
        s.startServer(5000);
    }
}