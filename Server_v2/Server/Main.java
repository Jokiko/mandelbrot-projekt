package Server;

public class Main {

    /**
     * main()
     * ruft Server.startServer() auf
     * @param args String[]
     */
    public static void main(String[] args){
    	Server server;
        server = new Server(5000);
        server.startServer();
    }
}