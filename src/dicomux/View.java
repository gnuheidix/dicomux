package dicomux;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.ResourceBundle;

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
	 * the model which serves as event listener
	 */
	private IController m_controller = null;
	
	/**
	 * The global language setting for the view
	 * (add etc to classpath;
	 * Run->Run Configuration->Classpath->User Entries dicomux-> Advanced...-> Add Folder -> etc)
	 */
	private ResourceBundle m_languageBundle;
	
	@Override
	public void registerModel(IModel model) {
		m_model = model;
	}
	
	@Override
	public void notifyView() {
		refreshAllTabs();
	}
	
	@Override
	public void registerController(IController controller) {
		m_controller = controller;
	}
	
	@Override
	public int getActiveWorkspaceId() {
		return m_tabbedPane.getSelectedIndex();
	}
	
	@Override
	public Locale getLanguage() {
		return m_languageBundle.getLocale();
	}
	
	/**
	 * creates a new view
	 */
	public View() {
		initialize();
	}
	
	/**
	 * initializes all components of the view
	 */
	private void initialize() {
		setTitle("Dicomux");
		setPreferredSize(new Dimension(800, 600));
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		initializeLanguage();
		
		// extract own contentPane and set its layout manager
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout(0, 5));
		
		// create a main menu and add it to the View
		m_menuBar = new JMenuBar();
		setJMenuBar(m_menuBar);
		
		// add menu entries to the main menu
		addFileMenu();
		addPluginMenu();
		addLanguageMenu();
		addHelpMenu();
		
		// create a tabbed pane and add it to the content pane
		m_tabbedPane = new JTabbedPane();
		m_tabbedPane.setTabPlacement(JTabbedPane.TOP);
		contentPane.add(m_tabbedPane, BorderLayout.CENTER);
		
		// display the frame in the middle of the screen
		pack();
		Point screenCenterPoint = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
		setLocation(new Point (screenCenterPoint.x - getSize().width / 2,
								screenCenterPoint.y - getSize().height / 2));
		setVisible(true);
	}
	
	//TODO implement
	/**
	 * initializes all language settings by checking the config file
	 */
	private void initializeLanguage() {
		// check whether there is a configuration file with a last setting for the language
			// yes: load that
			// no: use system setting
		m_languageBundle = ResourceBundle.getBundle("language", Locale.ENGLISH);
		
		UIManager.put("FileChooser.cancelButtonText", m_languageBundle.getString("cancelButtonText"));
		UIManager.put("FileChooser.openButtonText", m_languageBundle.getString("openButtonText"));
		UIManager.put("FileChooser.lookInLabelText", m_languageBundle.getString("lookInLabelText"));
		// ... add more of these calls in order to localize the whole thing
	}
	
	//TODO needs localization
	/**
	 * a convenience method for adding a file menu to the main menu
	 */
	private void addFileMenu() {
		JMenu menu = new JMenu(m_languageBundle.getString("key_file"));
		JMenuItem tmp = new JMenuItem(m_languageBundle.getString("key_openFile"));
		tmp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				m_controller.openDicomFileDialog();
			}
		});
		menu.add(tmp);
		tmp = new JMenuItem(m_languageBundle.getString("key_openDir"));
		tmp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				m_controller.openDicomDirectoryDialog();
			}
		});
		menu.add(tmp);
		menu.addSeparator();
		tmp = new JMenuItem(m_languageBundle.getString("key_closeTab"));
		tmp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				m_controller.closeWorkspace();
			}
		});
		menu.add(tmp);
		tmp = new JMenuItem(m_languageBundle.getString("key_closeAllTabs"));
		tmp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				m_controller.closeAllWorkspaces();
			}
		});
		menu.add(tmp);
		menu.addSeparator();
		tmp = new JMenuItem(m_languageBundle.getString("key_exit"));
		tmp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				m_controller.closeApplication();
			}
		});
		menu.add(tmp);
		m_menuBar.add(menu);
	}
	
	//TODO implement
	//TODO needs localization
	/**
	 * a convenience method for adding a menu for plugin selection to the main menu
	 */
	private void addPluginMenu() {
		JMenu menu = new JMenu(m_languageBundle.getString("key_view"));
		JMenuItem tmp = new JMenuItem("implement me!");
		tmp.setEnabled(false);
		menu.add(tmp);
		m_menuBar.add(menu);
	}
	
	//TODO needs localization
	//TODO implement
	/**
	 * a convenience method for adding a menu for language selection to the main menu
	 */
	private void addLanguageMenu() {
		final ButtonGroup bg = new ButtonGroup(); // is needed to make sure that only one radiobutton is set
		ActionListener langAL = new ActionListener() { // the action listener for all language change actions
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// implement me
				setLanguage(Locale.ENGLISH);
			}
		};
		
		JMenu menu = new JMenu(m_languageBundle.getString("key_language"));
		JMenuItem tmp = new JRadioButtonMenuItem(m_languageBundle.getString("key_english"), true);
		tmp.addActionListener(langAL);
		bg.add(tmp);
		menu.add(tmp);
		//TODO add more languages here
		
		menu.addSeparator();
		tmp = new JMenuItem(m_languageBundle.getString("key_languageNotification"));
		tmp.setEnabled(false);
		menu.add(tmp);
		
		m_menuBar.add(menu);
	}
	
	//TODO implement
	/**
	 * checks which language is selected in the language menu and writes the configuration
	 * to the language configuration file which will be loaded at the start of the application
	 */
	private void setLanguage(Locale locale) {
		// write new setting to configuration file
		// this one gets loaded at the next launch of dicomux
	}
	
	//TODO needs localization
	/**
	 * a convenience method for adding a help menu to the main menu
	 */
	private void addHelpMenu() {
		JMenu menu = new JMenu(m_languageBundle.getString("key_help"));
		JMenuItem tmp = new JMenuItem(m_languageBundle.getString("key_about"));
		menu.add(tmp);
		m_menuBar.add(menu);
	}
	
	/**
	 * a convenience method for checking whether a model is registered
	 * @return true or false
	 */
	private boolean isModelRegistered() {
		return m_model != null;
	}
	
	//TODO needs to be extended
	//TODO needs localization
	/**
	 * convenience method for fetching new information from the model
	 * this might be a bit expensive if there are many open tabs
	 */
	private void refreshAllTabs() {
		m_tabbedPane.removeAll();
		if (isModelRegistered()) {
			for (int i = 0; i < m_model.getWorkspaceCount(); ++i) {
				switch (m_model.getWorkspace(i).getTabState()) {
				case WELCOME: m_tabbedPane.add(m_languageBundle.getString("key_welcome"), makeWelcomeTab()); break;
				case FILE_OPEN: m_tabbedPane.add("Open file", makeOpenFileTab()); break;
				case ERROR_OPEN: m_tabbedPane.add("Error", makeErrorOpenTab()); break;
				}
			}
		}
	}
	
	//TODO localization needed
	//TODO cleanup
	/**
	 * convenience method for adding the welcome tab
	 * @return a JPanel
	 */
	protected JComponent makeWelcomeTab() {
		JPanel content = new JPanel(new BorderLayout(5 , 5), false);
		JPanel contentHead = new JPanel(new BorderLayout(5, 0), false);
		content.add(contentHead, BorderLayout.NORTH);
		
		JPanel message = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0),false);
		String text = m_languageBundle.getString("key_welcomeHtml");
		JLabel filler = new JLabel(text);
		message.add(filler, BorderLayout.WEST);
		contentHead.add(message, BorderLayout.NORTH);
		
		JPanel control = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0), false);
		JButton tmp = new JButton(m_languageBundle.getString("key_welcomeOpenFile"));
		tmp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				m_controller.openDicomFileDialog();
				
			}
		});
		control.add(tmp);
		
		tmp = new JButton(m_languageBundle.getString("key_welcomeOpenDir"));
		tmp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				m_controller.openDicomDirectoryDialog();
				
			}
		});
		control.add(tmp);
		contentHead.add(control, BorderLayout.SOUTH);
		
		return content;
	}
	
	//TODO localization needed
	/**
	 * convenience method for adding the welcome tab
	 * @return a JPanel
	 */
	protected JComponent makeOpenFileTab() {
		JPanel content = new JPanel(new BorderLayout(5 , 5), false);
		JPanel contentHead = new JPanel(new BorderLayout(5, 0), false);
		content.add(contentHead, BorderLayout.NORTH);
		
		JPanel message = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0),false);
		String text = "<html><font size=\"+2\">Open file</font><br/><i>Please select the DICOM file, you want to open.</i><br/><br/></html>";
		JLabel filler = new JLabel(text);
		message.add(filler, BorderLayout.WEST);
		contentHead.add(message, BorderLayout.NORTH);
		
		JPanel control = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0), false);
		JFileChooser filechooser = new JFileChooser();
		filechooser.setDialogType(JFileChooser.OPEN_DIALOG);
		filechooser.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = (JFileChooser) e.getSource();
				if (JFileChooser.APPROVE_SELECTION.equals(e.getActionCommand()))
					m_controller.openDicomFile(chooser.getSelectedFile().getPath());
				else if (JFileChooser.CANCEL_SELECTION.equals(e.getActionCommand()))
					m_controller.closeWorkspace();
			}
		});
		control.add(filechooser);
		contentHead.add(control, BorderLayout.SOUTH);
		
		return content;
	}
	
	//TODO localization needed
	/**
	 * convenience method for adding the welcome tab
	 * @return a JPanel
	 */
	protected JComponent makeErrorOpenTab() {
		JPanel content = new JPanel(new BorderLayout(5 , 5), false);
		JPanel contentHead = new JPanel(new BorderLayout(5, 0), false);
		content.add(contentHead, BorderLayout.NORTH);
		
		JPanel message = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0),false);
		String text = "<html><font size=\"+2\">Error</font><br/><i>Dicomux was unable to open the file.</i><br/><br/>You may want to do one of the following things:</html>";
		JLabel filler = new JLabel(text);
		message.add(filler, BorderLayout.WEST);
		contentHead.add(message, BorderLayout.NORTH);
		
		JPanel control = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0), false);
		JButton tmp = new JButton("Open DICOM file");
		tmp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				m_controller.openDicomFileDialog();
				
			}
		});
		control.add(tmp);
		
		tmp = new JButton("Open DICOM directory");
		tmp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				m_controller.openDicomDirectoryDialog();
				
			}
		});
		control.add(tmp);
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
			System.err.println(m_languageBundle.getString("key_err_loadFile") + path);
			return null;
		}
	}
}