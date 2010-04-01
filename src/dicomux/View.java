package dicomux;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class View extends JFrame {

	private static final long serialVersionUID = -3586989981842552511L;

	public View() {
		setTitle("dicomux");
		JTabbedPane tabbedPane = new JTabbedPane();
		JComponent panel1 = makeTextPanel("Panel #1");
		tabbedPane.addTab("Tab 1", panel1);
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

		JComponent panel2 = makeTextPanel("Panel #2");
		tabbedPane.addTab("Tab 2", panel2);
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

		JComponent panel3 = makeTextPanel("Panel #3");
		tabbedPane.addTab("Tab 3", panel3);
		tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);

		JComponent panel4 = makeTextPanel("Panel #4 (has a preferred size of 410 x 50).");
		panel4.setPreferredSize(new Dimension(410, 50));
		tabbedPane.addTab("Tab 4", panel4);
		tabbedPane.setMnemonicAt(3, KeyEvent.VK_4);
		tabbedPane.setTabPlacement(JTabbedPane.LEFT);
		add(tabbedPane);
	}
	
	protected JComponent makeTextPanel(String text) {
		JPanel panel = new JPanel(false);
		JLabel filler = new JLabel(text);
		filler.setHorizontalAlignment(JLabel.CENTER);
		panel.setLayout(new GridLayout(1, 1));
		panel.add(filler);
		return panel;
	}
}
