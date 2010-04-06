package dicomux;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * View for Dicomux
 * @author heidi
 *
 */
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
		m_tabbedPane.addTab("Welcome", makeWelcomePanel());
		m_tabbedPane.setMnemonicAt(0, KeyEvent.VK_W);
		// #################################################
		
		// display the frame in the middle of the screen
		pack();
		Point screenCenterPoint = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
		setLocation(new Point (screenCenterPoint.x - getSize().width / 2,
								screenCenterPoint.y - getSize().height / 2));
		setVisible(true);
	}
	
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
		String text = "<html><font size=\"+2\">Welcome to Dicomux</font><br/><i>The free viewer for DICOM files.</i><br/><br/>You may want to do one of the following things:</html>";
		JLabel filler = new JLabel(text);
		message.add(filler, BorderLayout.WEST);
		contentHead.add(message, BorderLayout.NORTH);
		
		JPanel control = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0), false);
		control.add(new JButton("Open DICOM file"));
		control.add(new JButton("Open DICOM directory"));
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