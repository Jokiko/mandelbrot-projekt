import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyboardListener implements KeyListener {

    /**
     * keyReleased()
     * wenn Keyboardtaste losgelassen wird entsprechendes ausgefuehrt
     * @param e KeyEvent
     */
    @Override
    public void keyReleased(KeyEvent e) {
        switch(e.getKeyCode()){
            case KeyEvent.VK_MINUS:
                MethodsUI.zoomOut(0.2);
                break;
            case KeyEvent.VK_PLUS:
                MethodsUI.zoomIn(0.2);
                break;
            case KeyEvent.VK_LEFT:
                MethodsUI.xMove -= 50;
                /*ServerThread.sendMessageText("Left/.../50");
                ServerThreadWebSocket.sendMessageText("Left/.../50");//*/

                MethodsUI.plotValRe();
                break;
            case KeyEvent.VK_RIGHT:
                MethodsUI.xMove += 50;
                /*ServerThread.sendMessageText("Right/.../50");
                ServerThreadWebSocket.sendMessageText("Right/.../50");//*/

                MethodsUI.plotValRe();
                break;
            case KeyEvent.VK_UP:
                MethodsUI.yMove -= 50;
                /*ServerThread.sendMessageText("Up/.../50");
                ServerThreadWebSocket.sendMessageText("Up/.../50");//*/

                MethodsUI.plotValRe();
                break;
            case KeyEvent.VK_DOWN:
                MethodsUI.yMove += 50;
                /*ServerThread.sendMessageText("Down/.../50");
                ServerThreadWebSocket.sendMessageText("Down/.../50");//*/

                MethodsUI.plotValRe();
                break;
            case KeyEvent.VK_ESCAPE:
                MethodsUI.restart();
                break;
            default:
                break;
        }
    }

    /**
     * keyTyped()
     * @IGNORE
     * @param e KeyEvent
     */
    @Override
    public void keyTyped(KeyEvent e) {}

    /**
     * keyPressed()
     * @IGNORE
     * @param e KeyEvent
     */
    @Override
    public void keyPressed(KeyEvent e) {}
}
