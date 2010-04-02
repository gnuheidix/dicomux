package dicomux;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class View extends JFrame {

	private static final long serialVersionUID = -3586989981842552511L;
	private JTabbedPane m_tabbedPane;
	private JMenuBar m_menuBar;
	private JPanel m_patientPanel;

	public View() {
		setTitle("dicomux");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// extract own contentPane and set its layout manager
		Container contentPane = getContentPane();
		SpringLayout layout = new SpringLayout();
		contentPane.setLayout(layout);
		
		// create a main menu and fill in some entries
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
		
		// create a tabbed pane and add it to the content pane
		m_tabbedPane = new JTabbedPane();
		m_tabbedPane.setPreferredSize(new Dimension(300, 200));
		JComponent panel1 = makeTextPanel("Panel #1");
//		panel1.setPreferredSize(new Dimension(200, 200));
		m_tabbedPane.addTab("Tab 1", panel1);
		m_tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
		m_tabbedPane.setTabPlacement(JTabbedPane.LEFT);
		contentPane.add(m_tabbedPane);
		
		// create a panel for the patient data and add it to the content pane
		m_patientPanel = new JExpander();
//		m_patientPanel.setPreferredSize(new Dimension(50, 50));
		contentPane.add(m_patientPanel);
		
		// adjust layout constraints of the components
		SpringLayout.Constraints contentPaneCons = layout.getConstraints(contentPane);
		SpringLayout.Constraints tabbedPaneCons = layout.getConstraints(m_tabbedPane);
		SpringLayout.Constraints patientPaneCons = layout.getConstraints(m_patientPanel);
		contentPaneCons.setHeight(Spring.sum(patientPaneCons.getConstraint(SpringLayout.HEIGHT),
												tabbedPaneCons.getConstraint(SpringLayout.HEIGHT)));
		contentPaneCons.setWidth(Spring.sum(patientPaneCons.getConstraint(SpringLayout.WIDTH),
												tabbedPaneCons.getConstraint(SpringLayout.WIDTH)));
		patientPaneCons.setX(Spring.sum(Spring.constant(5), tabbedPaneCons.getConstraint(SpringLayout.HEIGHT)));
//		patientPaneCons.setY(Spring.sum(Spring.constant(5), tabbedPaneCons.getConstraint(SpringLayout.WIDTH)));
		
//		setContainerSize(contentPane, 5);
		// display the frame in the middle of the screen
		pack();
		Point screenCenterPoint = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
		setLocation(new Point (screenCenterPoint.x - getSize().width / 2,
								screenCenterPoint.y - getSize().height / 2));
		setVisible(true);
	}
	
	protected class JExpander extends JPanel {
		private static final long serialVersionUID = -6318804811000861743L;
		private JButton m_toggle;
		private JPanel m_control;
		private JPanel m_content;
		private boolean expanded;
		
		public JExpander() {
			// set BorderLayout as main layout and activate double buffering
			super(new BorderLayout(5, 5), true);
			expanded = false;
			
			// create control panel with BorderLayout and add it
			m_control = new JPanel(new BorderLayout(), true);
			m_toggle = new JButton("Toggle");
			m_toggle.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					toggle();
				}
			});
			m_control.add(m_toggle, BorderLayout.WEST);
			
			// create content panel with GridLAyout and add it
			GridLayout layout = new GridLayout();
			layout.setHgap(5);
			layout.setVgap(5);
			m_content = new JPanel(new GridLayout(), true);
		}
		
		public void toggle () {
			m_content.setVisible(!expanded);
			expanded = !expanded;
			pack();
		}
		
		public void expand() {
			if (!expanded) {
				toggle();
			}
		}
		
		public void reduce() {
			if (expanded) {
				toggle();
			}
		}
		
		
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