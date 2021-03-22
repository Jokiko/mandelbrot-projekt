package src.Server;

public class Main {

    /**
     * main()
     * calls server.startServer()
     * @param args String[]
     */
    public static void main(String[] args){
    	Server server;
        server = new Server(5000);
        server.startServer();
    }
}