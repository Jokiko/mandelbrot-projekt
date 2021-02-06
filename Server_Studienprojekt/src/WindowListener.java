import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class WindowListener extends WindowAdapter {
    @Override
    public void windowClosing(WindowEvent e) {
        ServerThread.sendMessageText("disconnect");
        ServerThreadWebSocket.sendMessageText("disconnect");
        System.out.println("closing");
    }
}
