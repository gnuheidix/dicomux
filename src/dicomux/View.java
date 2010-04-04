package dicomux;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

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
//		m_tabbedPane.setVisible(false);
		contentPane.add(m_tabbedPane);
		
		// create a panel for the patient data and add it to the content pane
		m_patientPanel = new JExpander(false, false);
		contentPane.add(m_patientPanel);
		
		// extract layout constraints from the components
		SpringLayout.Constraints contentPaneCons = layout.getConstraints(contentPane);
		SpringLayout.Constraints tabbedPaneCons = layout.getConstraints(m_tabbedPane);
		SpringLayout.Constraints patientPaneCons = layout.getConstraints(m_patientPanel);
		
		// adjust layout constraints of the contentPane
		contentPaneCons.setHeight(Spring.sum(Spring.sum(patientPaneCons.getConstraint(SpringLayout.HEIGHT),
														tabbedPaneCons.getConstraint(SpringLayout.HEIGHT)),
											Spring.constant(5)));
		contentPaneCons.setWidth(Spring.sum(Spring.max(patientPaneCons.getConstraint(SpringLayout.WIDTH),
														tabbedPaneCons.getConstraint(SpringLayout.WIDTH)),
											Spring.constant(10)));
		
		// adjust layout constraints of the tabbedPane
		tabbedPaneCons.setX(Spring.sum(contentPaneCons.getConstraint(SpringLayout.WEST),Spring.constant(5)));
		
		// adjust layout constraints of the patientPane
		patientPaneCons.setX(Spring.sum(contentPaneCons.getConstraint(SpringLayout.WEST), Spring.constant(5)));
		patientPaneCons.setY(Spring.sum(tabbedPaneCons.getConstraint(SpringLayout.SOUTH), Spring.constant(5)));
		
		// display the frame in the middle of the screen
		pack();
		Point screenCenterPoint = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
		setLocation(new Point (screenCenterPoint.x - getSize().width / 2,
								screenCenterPoint.y - getSize().height / 2));
		setVisible(true);
	}
	
	/**
	 * A JPanel derivative with a button for setting the content to visible or invisible
	 * @author heidi
	 *
	 */
	protected class JExpander extends JPanel {
		private static final long serialVersionUID = -6318804811000861743L;
		private JButton m_toggle;
		private JPanel m_control;
		private JPanel m_content;
		private boolean m_expanded;
		
		/**
		 * initializes all components of the JExpander
		 */
		protected void initComponents(boolean isDoubleBuffered) {
			// create control panel with BorderLayout and add it
			m_control = new JPanel(new BorderLayout(), isDoubleBuffered);
			m_toggle = new JButton();
			m_toggle.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					toggle();
				}
			});
			m_control.add(m_toggle, BorderLayout.WEST); // add control button to a certain position
			add(m_control, BorderLayout.NORTH);
			add(new JLabel(),BorderLayout.CENTER);
			
			// create content panel with SpringLayout
			GridLayout layout = new GridLayout();
			m_content = new JPanel(layout, isDoubleBuffered);

			// Dummy content
			m_content.add(new JLabel("Name: _______"));
			m_content.add(new JLabel("Geburtsdatum: _______"));
			add(m_content, BorderLayout.SOUTH);
		}
		
		/**
		 * creates a new JExpander object
		 * @param expanded determins wheather the new expander should be in expanded or shrinked mode
		 */
		public JExpander(boolean expanded, boolean isDoubleBuffered) {
			// set BorderLayout as main layout and activate double buffering
			super(new BorderLayout(), isDoubleBuffered);
			
			// initialize all components
			initComponents(isDoubleBuffered);
			
			// switch to initial expander mode
			setState(expanded);
		}
		
		/**
		 * inverts the state of the expander
		 */
		public void toggle() {
			setState(!m_expanded);
		}
		
		/**
		 * sets the state of the expander directly
		 * @param expanded
		 */
		public void setState(boolean expanded) {
			m_expanded = expanded;
			m_content.setVisible(m_expanded);
			m_toggle.setText(m_expanded ? "Patientendaten verbergen" : "Patientendaten anzeigen");
		}
	}

	/**
	 * convenience method for testing purpose
	 * @param text String which will be shown in the center of the panel
	 * @return a JPanel
	 */
	protected JComponent makeTextPanel(String text) {
		JPanel panel = new JPanel(false);
		JLabel filler = new JLabel(text);
		filler.setHorizontalAlignment(JLabel.CENTER);
		panel.setLayout(new GridLayout(1, 1));
		panel.add(filler);
		return panel;
	}
}