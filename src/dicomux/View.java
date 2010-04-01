package dicomux;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class View extends JFrame {

	private static final long serialVersionUID = -3586989981842552511L;
	private JTabbedPane m_tabbedPane;
	private JMenuBar m_menuBar;

	public View() {
		setTitle("dicomux");
		m_menuBar = new JMenuBar();
		JMenu menu = new JMenu("File");
		menu.add(new JMenuItem("Open"));
		menu.addSeparator();
		menu.add(new JMenuItem("Exit"));
		m_menuBar.add(menu);
		
		menu = new JMenu("Help");
		menu.add(new JMenuItem("About"));
		m_menuBar.add(menu);
		setJMenuBar(m_menuBar);
		
		m_tabbedPane = new JTabbedPane();
		JComponent panel1 = makeTextPanel("Panel #1");
		m_tabbedPane.addTab("Tab 1", panel1);
		m_tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

		JComponent panel2 = makeTextPanel("Panel #2");
		panel2.setPreferredSize(new Dimension(800, 600));
		m_tabbedPane.addTab("Tab 2", panel2);
		m_tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

		m_tabbedPane.setTabPlacement(JTabbedPane.LEFT);
		add(m_tabbedPane);
		
		pack();
		
		Point screenCenterPoint = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
		setLocation(new Point (screenCenterPoint.x - getSize().width / 2,
								screenCenterPoint.y - getSize().height / 2));
		setVisible(true);
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
