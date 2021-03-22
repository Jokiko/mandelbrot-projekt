package src.Panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class ContentPanel extends JPanel {
	
	private final GridBagLayout gridBagLayout = new GridBagLayout();
	private final GridBagConstraints gridBagConstraints = new GridBagConstraints();

	public ContentPanel() {
		setLayout(gridBagLayout);
		setBorder(BorderFactory.createLineBorder(Color.WHITE));
		setBackground(Color.DARK_GRAY);
		setupGBC();
	}

	private void setupGBC() {

		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.NORTH;
		gridBagConstraints.insets = new Insets(2, 2, 2, 2);
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = 0;
		gridBagConstraints.gridheight = 0;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.ipadx = 0;
		gridBagConstraints.ipady = 0;

	}

	public void setLocation(int gridx, int gridy, int gridwidth, int gridheight) {
		gridBagConstraints.gridx = gridx;
		gridBagConstraints.gridy = gridy;
		gridBagConstraints.gridwidth = gridwidth;
		gridBagConstraints.gridheight = gridheight;
	}

	public void addComponent(Component component) {
		add(component, gridBagConstraints);
		setupGBC();
	}
}
