import java.io.PrintWriter;

public class MethodsServerThread {

    /**
     * sendMessage()
     * @param os PrintWriter
     * @param message String
     */
    public synchronized static void sendMessage(PrintWriter os, String message){
        try {
            os.println(message);
            os.flush();
            System.out.println("send \"" + message + "\" to Client");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
