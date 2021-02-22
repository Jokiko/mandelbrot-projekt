package src.Listener;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import src.Server.Server;

public class WindowListener extends WindowAdapter {

	Server server;

	public WindowListener(Server server) {
		this.server = server;
	}

	@Override
	public void windowClosing(WindowEvent e) {
		System.out.println("Closing");
		server.close();
	}
}
