package dicomux;

import java.awt.*;
import javax.swing.*;

/**
 * View for Dicomux
 * @author heidi
 *
 */
public class View extends JFrame implements IView {
	private static final long serialVersionUID = -3586989981842552511L;
	
	/**
	 * contains the tabbed pane which holds all workspaces
	 */
	private JTabbedPane m_tabbedPane;
	
	/**
	 * contains the menu bar of the application
	 */
	private JMenuBar m_menuBar;
	
	/**
	 * the model which serves as data source
	 */
	private IModel m_model = null;
	
	/**
	 * creates a new view
	 */
	public View() {
		initialize();
	}
	
	//TODO localization needed
	/**
	 * initializes all components of the view
	 */
	private void initialize() {
		setTitle("Dicomux");
		setPreferredSize(new Dimension(800, 600));
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
		
		// display the frame in the middle of the screen
		pack();
		Point screenCenterPoint = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
		setLocation(new Point (screenCenterPoint.x - getSize().width / 2,
								screenCenterPoint.y - getSize().height / 2));
	}
	
	/**
	 * removes all tabs from the view / handle with care
	 */
	public void clearTabs() {
		m_tabbedPane.removeAll();
	}
	
	//TODO needs to be extended
	/**
	 * adds a new TabObject to the View / this creates a new workspace
	 * @param tab
	 */
	public void refreshAllTabs() {
		if (isModelRegistered()) {
			for (int i = 0; i < m_model.getWorkspaceCount(); ++i) {
				switch (m_model.getWorkspace(i).getTabState()) {
				case WELCOME: m_tabbedPane.add("Welcome", makeWelcomeTab());
				}
			}
		}
	}
	
	@Override
	public void notifyView() {
		refreshAllTabs();
	}

	//TODO localization needed / cleanup
	/**
	 * convenience method for adding the welcome tab
	 * @return a JPanel
	 */
	protected JComponent makeWelcomeTab() {
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

	public void registerModel(Model model) {
		m_model = model;
	}
	
	private boolean isModelRegistered() {
		return m_model != null;
	}
}