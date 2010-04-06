package dicomux;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class View extends JFrame {
	private static final long serialVersionUID = -3586989981842552511L;
	
	/**
	 * contains the workspace tabbed pane
	 */
	private JTabbedPane m_tabbedPane;
	
	/**
	 * contains the menu bar of the application
	 */
	private JMenuBar m_menuBar;
	
//	/**
//	 * contains the expander for the patient specific data
//	 */
//	private JExpander m_patientPanel;
	
	//TODO outsource component initialization
	/**
	 * creates a new view
	 */
	public View() {
		setTitle("Dicomux");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// extract own contentPane and set its layout manager
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout(0, 5));
		
		// create a main menu and add it to the View
		m_menuBar = new JMenuBar();
		setJMenuBar(m_menuBar);
		
		// ####### add dummy entries ######
		//TODO remove
		JMenu menu = new JMenu("File");
		menu.add(new JMenuItem("Open"));
		menu.addSeparator();
		menu.add(new JMenuItem("Exit"));
		m_menuBar.add(menu);
		
		menu = new JMenu("Help");
		menu.add(new JMenuItem("About"));
		m_menuBar.add(menu);
		// ################################
		
		// create a tabbed pane and add it to the content pane
		m_tabbedPane = new JTabbedPane();
		m_tabbedPane.setTabPlacement(JTabbedPane.TOP);
		contentPane.add(m_tabbedPane, BorderLayout.CENTER);
		
		// ########### add a dummy tab #####################
		//TODO remove
		JComponent panel1 = makeWelcomePanel();
		m_tabbedPane.addTab("Welcome", panel1);
		m_tabbedPane.setMnemonicAt(0, KeyEvent.VK_W);
		// #################################################
		
//		// create a panel for the patient data and add it to the content pane
//		m_patientPanel = new JExpander(false, false);
//		contentPane.add(m_patientPanel, BorderLayout.SOUTH);

		// display the frame in the middle of the screen
		pack();
		Point screenCenterPoint = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
		setLocation(new Point (screenCenterPoint.x - getSize().width / 2,
								screenCenterPoint.y - getSize().height / 2));
		setVisible(true);
	}
	
//	/**
//	 * A JPanel derivative with a button for setting the content to visible or invisible
//	 * @author heidi
//	 *
//	 */
//	protected class JExpander extends JPanel {
//		private static final long serialVersionUID = -6318804811000861743L;
//		
//		/**
//		 * button for changing the mode of JExpander
//		 */
//		private JButton m_toggle;
//		
//		/**
//		 * contains the toggle 
//		 */
//		private JPanel m_control;
//		
//		/**
//		 * contains all content data
//		 */
//		private JPanel m_content;
//		
//		/**
//		 * current state of the expander
//		 */
//		private boolean m_expanded;
//		
//		//TODO outsource the EventHandler
//		/**
//		 * initializes all components of the JExpander
//		 */
//		protected void initComponents(boolean isDoubleBuffered) {
//			// create control panel with BorderLayout and add it to JExpander
//			m_control = new JPanel(new BorderLayout(), isDoubleBuffered);
//			m_toggle = new JButton();
//			m_toggle.addActionListener(new ActionListener() {
//				@Override
//				public void actionPerformed(ActionEvent e) {
//					toggle();
//				}
//			});
//			m_control.add(m_toggle, BorderLayout.WEST); // add control button to a certain position
//			add(m_control, BorderLayout.NORTH);
//			
//			// create content panel with GridLayout and add it to JExpander
//			GridLayout layout = new GridLayout();
//			m_content = new JPanel(layout, isDoubleBuffered);
//			add(m_content, BorderLayout.SOUTH);
//			
//			// ############ add dummy content ####################
//			m_content.add(new JLabel("Name: _______"));
//			m_content.add(new JLabel("Geburtsdatum: _______"));
//			// ###################################################
//		}
//		
//		/**
//		 * creates a new JExpander object
//		 * @param expanded determins wheather the new expander should be in expanded or shrinked mode
//		 */
//		public JExpander(boolean expanded, boolean isDoubleBuffered) {
//			// set BorderLayout as main layout and set double buffering
//			super(new BorderLayout(), isDoubleBuffered);
//			
//			// initialize all components
//			initComponents(isDoubleBuffered);
//			
//			// switch to initial JExpander mode
//			setState(expanded);
//		}
//		
//		/**
//		 * inverts the state of the expander
//		 */
//		public void toggle() {
//			setState(!m_expanded);
//		}
//		
//		//TODO localization needed
//		/**
//		 * sets the state of the expander directly
//		 * @param expanded
//		 */
//		public void setState(boolean expanded) {
//			m_expanded = expanded;
//			m_content.setVisible(m_expanded);
//			m_toggle.setText(m_expanded ? "Hide patient data" : "Show patient data");
//		}
//	}
	
	//TODO localization needed / cleanup
	/**
	 * convenience method for testing purpose
	 * @return a JPanel
	 */
	protected JComponent makeWelcomePanel() {
		JPanel content = new JPanel(new BorderLayout(5 , 5), false);
		JPanel contentHead = new JPanel(new BorderLayout(5, 0), false);
		content.add(contentHead, BorderLayout.NORTH);
		
		JPanel message = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0),false);
		String text = "<html><font size=\"+2\">Welcome to Dicomux</font><br/><i>The free ECG viewer for DICOM files.</i><br/><br/>You may want to do one of the following things:</html>";
		JLabel filler = new JLabel(text);
		message.add(filler, BorderLayout.WEST);
		contentHead.add(message, BorderLayout.NORTH);
		
		JPanel control = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0), false);
		control.add(new JButton("Open DICOM file")/*, BorderLayout.WEST*/);
		control.add(new JButton("Open DICOM directory")/*, BorderLayout.EAST*/);
		contentHead.add(control, BorderLayout.SOUTH);
		
		return content;
	}
	
	/**
	 * convenience method for opening ImageIcons // copied from http://java.sun.com/docs/books/tutorial/uiswing/components/icon.html
	 * @param path path to the icon file
	 * @param description description of the file
	 * @return the ImageIcon of the opened file or null
	 */
	protected ImageIcon createImageIcon(String path, String description) {
		java.net.URL imgURL = getClass().getResource(path);
		if (imgURL != null)
			return new ImageIcon(imgURL, description);
		else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}
}