import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class pictureListener{
    /**
     * Inner class pictureMouseListener
     */
    static class pictureMouseListener implements MouseListener {
        /**
         * Startwerte des Rechtecks werden festgelegt, wenn die linke
         * Maustaste gelickt wurde
         *
         * @param e MouseEvent
         */
        @Override
        public void mousePressed(MouseEvent e) {
            MethodsUI.mouseEventPressed(e);
        }

        /**
         * Hereinzoomen in eine bestimmten Bereich wird ausgeführt,
         * wenn linke Maustaste losgelassen wird
         *
         * @param e MouseEvent
         */
        @Override
        public void mouseReleased(MouseEvent e) {
            MethodsUI.mouseEventReleased(e);
        }

        /**
         * bricht mit rechtem Mausklick das Hereinzoomen in einen
         * bestimmten Bereich ab
         *
         * @param e MouseEvent
         */
        @Override
        public void mouseClicked(MouseEvent e) {
            MethodsUI.mouseEventClicked(e);
        }

        /**
         * mouseEntered()
         *
         * @param e MouseEvent
         * @IGNORE
         */
        @Override
        public void mouseEntered(MouseEvent e) {
        }

        /**
         * mouseExited()
         *
         * @param e MouseEvent
         * @IGNORE
         */
        @Override
        public void mouseExited(MouseEvent e) {
        }
    }

    /**
     * Inner class pictureMouseMotionListener
     */
    static class pictureMouseMotionListener implements MouseMotionListener {
        /**
         * zeichnet Rechteck mit Mittelkreuz, solange linke Maustaste geklickt
         * und bewegt wird
         *
         * @param e MouseEvent
         */
        @Override
        public void mouseDragged(MouseEvent e) {
            MethodsUI.mouseEventDragged(e);
        }

        /**
         * mouseMoved()
         *
         * @param e MouseEvent
         * @IGNORE
         */
        @Override
        public void mouseMoved(MouseEvent e) {}
    }
}
