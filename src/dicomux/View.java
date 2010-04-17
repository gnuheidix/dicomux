package dicomux;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

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
	private static IController m_controller = null;
	
	/**
	 * The global language setting for the view
	 */
	private static ResourceBundle m_languageBundle; 
	
	/**
	 * base name of the language files which are located in etc<br/>
	 * this constant will be used by m_languageBundle
	 */
	private final String m_langBaseName = "language";
	
	/**
	 * path to the language setting file
	 */
	private final String m_pathLanguageSetting = "etc/language.setting";
	
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
		initializeLanguage();
		initializeApplication();
	}
	
	/**
	 * initializes all components of the view
	 */
	private void initializeApplication() {
		setTitle("Dicomux");
		setPreferredSize(new Dimension(800, 600));
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
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
	
	//
	/**
	 * initializes all language settings by checking the config file
	 */
	private void initializeLanguage() {
		Locale locale; 
		
		File langConf = new File(m_pathLanguageSetting);
		BufferedReader br = null; // load that file
		try {
			br = new BufferedReader(new FileReader(langConf));
			String lang = br.readLine();
			locale= new Locale(lang);
			
		} catch (IOException e) { // use the system setting if it didn't work
			//e.printStackTrace();
			locale= new Locale(System.getProperty("user.language"));
		}
		// set the global language for all GUI Elements
		m_languageBundle = ResourceBundle.getBundle(m_langBaseName, locale);

		UIManager.put("FileChooser.cancelButtonText", m_languageBundle.getString("cancelButtonText"));
		UIManager.put("FileChooser.openButtonText", m_languageBundle.getString("openButtonText"));
		UIManager.put("FileChooser.lookInLabelText", m_languageBundle.getString("lookInLabelText"));
		// ... add more of these calls in order to localize the whole thing
	}
	
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
	
	/**
	 * a convenience method for adding a menu for language selection to the main menu
	 */
	private void addLanguageMenu() {
		ActionListener langAL = new ActionListener() { // the action listener for all language change actions
			@Override
			public void actionPerformed(ActionEvent arg0) {
				setLanguage(new Locale(arg0.getActionCommand()));
			}
		};
		
		JMenu menu = new JMenu(m_languageBundle.getString("key_language"));

		//TODO: Check Language File Content
		File dir = new File("etc");
		// get all language files from the etc folder which are in this format BundleName_xx.properties
		String[]lang_list = dir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				String regex = "^"+m_langBaseName+"_[a-z]{2}\\.properties$";
				boolean test = name.matches(regex);
				return test;
			}
		});
		//add all languages found in the language files to the language-selection menu
		for (String i : lang_list) {
			Locale l = new Locale(i.substring(i.indexOf("_")+1, i.indexOf(".")));
			JMenuItem tmp = new JMenuItem(l.getLanguage());
			tmp.addActionListener(langAL);
			menu.add(tmp);
		}
<<<<<<< HEAD
=======
		
		//TODO add more languages here
>>>>>>> 30ece2d3be7b3940fdbf9b5f132172ebe6939828
		menu.addSeparator();
		JMenuItem tmp = new JMenuItem(m_languageBundle.getString("key_languageNotification"));
		tmp.setEnabled(false);
		menu.add(tmp);
		
		m_menuBar.add(menu);
	}
	
	/**
	 * checks which language is selected in the language menu and writes the configuration
	 * to the language configuration file which will be loaded at the start of the application
	 */
	private void setLanguage(Locale locale) {
		String confFilePath = new String("etc/language.setting"); 
		FileWriter fw;
		try {
			fw = new FileWriter(confFilePath);
			fw.write(locale.getLanguage());
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
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
	/**
	 * convenience method for fetching new information from the model
	 * this might be a bit expensive if there are many open tabs
	 */
	private void refreshAllTabs() {
		m_tabbedPane.removeAll();
		if (isModelRegistered()) {
			for (int i = 0; i < m_model.getWorkspaceCount(); ++i) {
				switch (m_model.getWorkspace(i).getTabState()) {
				case WELCOME: m_tabbedPane.add(m_languageBundle.getString("key_welcome"), StaticDialogs.makeWelcomeTab()); break;
				case FILE_OPEN: m_tabbedPane.add("Open file", StaticDialogs.makeOpenFileTab()); break;
				case ERROR_OPEN: m_tabbedPane.add("Error", StaticDialogs.makeErrorOpenTab()); break;
				}
			}
		}
	}
	
	/**
	 * This class encapsulates all static dialogs and their convenience functions
	 * @author heidi
	 *
	 */
	private static class StaticDialogs {
		/**
		 * convenience method for building the welcome tab
		 * @return a JPanel
		 */
		protected static JComponent makeWelcomeTab() {
			JPanel content = new JPanel(new BorderLayout(5 , 5), false);
			JPanel contentHead = new JPanel(new BorderLayout(5, 0), false);
			content.add(contentHead, BorderLayout.NORTH);
			
			contentHead.add(makeMessage(m_languageBundle.getString("key_welcomeHtml")), BorderLayout.NORTH);
			contentHead.add(makeOpenButtons(), BorderLayout.SOUTH);
			
			return content;
		}
		
		/**
		 * convenience method for building the file open dialog tab
		 * @return a JPanel
		 */
		protected static JComponent makeOpenFileTab() {
			JPanel content = new JPanel(new BorderLayout(5 , 5), false);
			JPanel contentHead = new JPanel(new BorderLayout(5, 0), false);
			content.add(contentHead, BorderLayout.NORTH);
			
			contentHead.add(makeMessage(m_languageBundle.getString("key_html_openFile")), BorderLayout.NORTH);
			
			JPanel control = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0), false);
			JFileChooser filechooser = new JFileChooser();
			filechooser.setDialogType(JFileChooser.OPEN_DIALOG);
			// TODO: DateiName and DateiTyp not in actual Language ??
			filechooser.setLocale(m_languageBundle.getLocale());		// Set the actual language to the file chooser!
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
		
		/**
		 * convenience method for building the error open tab
		 * @return a JPanel
		 */
		protected static JComponent makeErrorOpenTab() {
			JPanel content = new JPanel(new BorderLayout(5 , 5), false);
			JPanel contentHead = new JPanel(new BorderLayout(5, 0), false);
			content.add(contentHead, BorderLayout.NORTH);
			
			contentHead.add(makeMessage(m_languageBundle.getString("key_html_errOpenFile")), BorderLayout.NORTH);
			contentHead.add(makeOpenButtons(), BorderLayout.SOUTH);
			
			return content;
		}
		
		/**
		 * convenience method for adding a headline to a static dialog
		 * @param msg the message - this might be HTML
		 * @return a JPanel with the message
		 */
		private static JComponent makeMessage(String msg) {
			JPanel retVal = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0),false);
			JLabel filler = new JLabel(msg);
			retVal.add(filler, BorderLayout.WEST);
			return retVal;
		}
		
		/**
		 * convenience method for adding open buttons to a static dialog
		 * @return a JPanel with open buttons
		 */
		private static JComponent makeOpenButtons() {
			JPanel retVal = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0), false);
			JButton tmp = new JButton(m_languageBundle.getString("key_welcomeOpenFile"));
			tmp.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					m_controller.openDicomFileDialog();
					
				}
			});
			retVal.add(tmp);
			
			tmp = new JButton(m_languageBundle.getString("key_welcomeOpenDir"));
			tmp.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					m_controller.openDicomDirectoryDialog();
					
				}
			});
			retVal.add(tmp);
			
			return retVal;
		}
		
//		/**
//		 * convenience method for opening ImageIcons // copied from http://java.sun.com/docs/books/tutorial/uiswing/components/icon.html
//		 * @param path path to the icon file
//		 * @param description description of the file
//		 * @return the ImageIcon of the opened file or null
//		 */
//		protected static ImageIcon createImageIcon(String path, String description) {
//			java.net.URL imgURL = getClass().getResource(path);
//			if (imgURL != null)
//				return new ImageIcon(imgURL, description);
//			else {
//				System.err.println(m_languageBundle.getString("key_err_loadFile") + path);
//				return null;
//			}
//		}
	}
}