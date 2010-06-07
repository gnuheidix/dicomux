package dicomux;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 * View for Dicomux
 * @author heidi
 * @author tobi
 *
 */
public class View extends JFrame implements IView {
	private static final long serialVersionUID = -3586989981842552511L;
	
	/**
	 * the path to the last selected folder
	 */
	private static String m_lastSelectedFilePath = null;
	
	/**
	 * contains the tabbed pane which holds all workspaces
	 */
	private static JTabbedPane m_tabbedPane;
	
	/**
	 * contains the menu bar of the application
	 */
	private JMenuBar m_menuBar;
	
	/**
	 * contains all suitable plug-ins for the currently opened DicomObject
	 */
	private JMenu m_pluginMenu;
	
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
	
	/**
	 * determins whether there is a refresh of the workspace in progress
	 */
	private static boolean m_refreshInProgress = false;
	
	/**
	 * Object for synchronizing the access to m_tabbedPane
	 */
	private final Object m_refreshLock = new Object(); 
	
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
		// misc initialization
		setTitle("Dicomux");
		setPreferredSize(new Dimension(800, 600));
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setIconImage(new ImageIcon("etc/images/logo.png").getImage());
		UIManager.put("FileChooser.readOnly", Boolean.TRUE);
		
		// extract own contentPane and set its layout manager
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout(0, 5));
		
		// create a main menu and add it to the View
		m_menuBar = new JMenuBar();
		setJMenuBar(m_menuBar);
		
		// add menu entries to the main menu
		initializeMenus();
		
		// create a tabbed pane, set a ChangeListener and add it to the content pane
		m_tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		m_tabbedPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				synchronized (m_refreshLock) {
					if (!m_refreshInProgress) {
						// get the index of the selected workspace
						int newWorkspaceIndex = m_tabbedPane.getSelectedIndex();
						
						// tell the controller what happened
						m_controller.setActiveWorkspace(newWorkspaceIndex);
						
						// update the plug-ins menu
						addPluginMenuEntries(m_model.getWorkspace(newWorkspaceIndex).getSuitablePlugins());
					}
				}
			}
		});
		contentPane.add(m_tabbedPane, BorderLayout.CENTER);
		
		// display the frame in the middle of the screen
		pack();
		Point screenCenterPoint = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
		setLocation(new Point (screenCenterPoint.x - getSize().width / 2,
								screenCenterPoint.y - getSize().height / 2));
		setVisible(true);
	}
	
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
		
		// set the global language for all GUI Elements (load the ResourceBundle)
		m_languageBundle = ResourceBundle.getBundle(m_langBaseName, locale);
		
		// set all localization entries for JFileChooser
		UIManager.put("FileChooser.openDialogTitleText", m_languageBundle.getString("openDialogTitleText"));
		UIManager.put("FileChooser.saveDialogTitleText", m_languageBundle.getString("saveDialogTitleText"));
		UIManager.put("FileChooser.saveInLabelText", m_languageBundle.getString("saveInLabelText"));
		UIManager.put("FileChooser.fileNameHeaderText", m_languageBundle.getString("fileNameHeaderText"));
		UIManager.put("FileChooser.fileSizeHeaderText", m_languageBundle.getString("fileSizeHeaderText"));
		UIManager.put("FileChooser.fileTypeHeaderText", m_languageBundle.getString("fileTypeHeaderText"));
		UIManager.put("FileChooser.fileDateHeaderText", m_languageBundle.getString("fileDateHeaderText"));
		UIManager.put("FileChooser.fileAttrHeaderText", m_languageBundle.getString("fileAttrHeaderText"));
		UIManager.put("FileChooser.directoryOpenButtonText", m_languageBundle.getString("directoryOpenButtonText"));
		UIManager.put("FileChooser.directoryOpenButtonToolTipText", m_languageBundle.getString("directoryOpenButtonToolTipText"));
		UIManager.put("FileChooser.acceptAllFileFilterText" , m_languageBundle.getString("acceptAllFileFilterText"));
		UIManager.put("FileChooser.cancelButtonText" , m_languageBundle.getString("cancelButtonText"));
		UIManager.put("FileChooser.cancelButtonToolTipText" , m_languageBundle.getString("cancelButtonToolTipText"));
		UIManager.put("FileChooser.detailsViewButtonAccessibleName" , m_languageBundle.getString("detailsViewButtonAccessibleName"));
		UIManager.put("FileChooser.detailsViewButtonToolTipText" , m_languageBundle.getString("detailsViewButtonToolTipText"));
		UIManager.put("FileChooser.directoryDescriptionText" , m_languageBundle.getString("directoryDescriptionText"));
		UIManager.put("FileChooser.fileDescriptionText" , m_languageBundle.getString("fileDescriptionText"));
		UIManager.put("FileChooser.fileNameLabelText" , m_languageBundle.getString("fileNameLabelText"));
		UIManager.put("FileChooser.filesOfTypeLabelText" , m_languageBundle.getString("filesOfTypeLabelText"));
		UIManager.put("FileChooser.helpButtonText" , m_languageBundle.getString("helpButtonText"));
		UIManager.put("FileChooser.helpButtonToolTipText" , m_languageBundle.getString("helpButtonToolTipText"));
		UIManager.put("FileChooser.homeFolderAccessibleName" , m_languageBundle.getString("homeFolderAccessibleName"));
		UIManager.put("FileChooser.homeFolderToolTipText" , m_languageBundle.getString("homeFolderToolTipText"));
		UIManager.put("FileChooser.listViewButtonAccessibleName" , m_languageBundle.getString("listViewButtonAccessibleName"));
		UIManager.put("FileChooser.listViewButtonToolTipText" , m_languageBundle.getString("listViewButtonToolTipText"));
		UIManager.put("FileChooser.lookInLabelText" , m_languageBundle.getString("lookInLabelText"));
		UIManager.put("FileChooser.newFolderAccessibleName" , m_languageBundle.getString("newFolderAccessibleName"));
		UIManager.put("FileChooser.newFolderErrorText" , m_languageBundle.getString("newFolderErrorText"));
		UIManager.put("FileChooser.newFolderToolTipText" , m_languageBundle.getString("newFolderToolTipText"));
		UIManager.put("FileChooser.openButtonText" , m_languageBundle.getString("openButtonText"));
		UIManager.put("FileChooser.openButtonToolTipText" , m_languageBundle.getString("openButtonToolTipText"));
		UIManager.put("FileChooser.saveButtonText" , m_languageBundle.getString("saveButtonText"));
		UIManager.put("FileChooser.saveButtonToolTipText" , m_languageBundle.getString("saveButtonToolTipText"));
		UIManager.put("FileChooser.updateButtonText" , m_languageBundle.getString("updateButtonText"));
		UIManager.put("FileChooser.updateButtonToolTipText" , m_languageBundle.getString("updateButtonToolTipText"));
		UIManager.put("FileChooser.upFolderAccessibleName" , m_languageBundle.getString("upFolderAccessibleName"));
		UIManager.put("FileChooser.upFolderToolTipText" , m_languageBundle.getString("upFolderToolTipText"));
	}
	
	/**
	 * initializes the whole main menu
	 */
	private void initializeMenus() {
		m_menuBar.removeAll();
		addFileMenu();
		addPluginMenu();
		addLanguageMenu();
		addHelpMenu();
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
	
	/**
	 * a convenience method for adding a menu for plugin selection to the main menu
	 */
	private void addPluginMenu() {
		m_pluginMenu = new JMenu(m_languageBundle.getString("key_view"));
		addPluginMenuEntries(null);
		m_menuBar.add(m_pluginMenu);
	}
	
	/**
	 * a convenience method for adding all suitable plug-ins of the currently opened workspace to the plug-in menu
	 * @param suitablePlugins
	 */
	private void addPluginMenuEntries(Vector<APlugin> suitablePlugins) {
		if (suitablePlugins == null || suitablePlugins.size() == 0) {
			m_pluginMenu.setEnabled(false);
			m_pluginMenu.removeAll();
		}
		else {
			m_pluginMenu.setEnabled(true);
			m_pluginMenu.removeAll();
			for (final APlugin i: suitablePlugins) {
				JMenuItem tmp = new JMenuItem(i.getName());
				tmp.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						m_controller.setActivePlugin(i.getName());
					}
				});
				m_pluginMenu.add(tmp);
			}
		}
	}
	
	// check for crap
	/**
	 * a convenience method for adding a menu for language selection to the main menu
	 */
	private void addLanguageMenu() {
		ActionListener langAL = new ActionListener() { // the action listener for all language change actions
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// build new Locale
				Locale newLocale = new Locale(arg0.getActionCommand());
				
				// set the language in the view
				setLanguage(newLocale);
				
				// inform the controller what happened
				m_controller.setLanguage(newLocale);
				
				// reinitialize the view
				initializeLanguage();
				initializeMenus();
				refreshAllTabs();
				repaint();
			}
		};
		
		JMenu menu = new JMenu(m_languageBundle.getString("key_language"));
		
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
		
		menu.addSeparator();
		JMenuItem tmp = new JMenuItem(m_languageBundle.getString("key_languageNotification"));
		tmp.setEnabled(false);
		menu.add(tmp);
		
		m_menuBar.add(menu);
	}
	
	/**
	 * checks which language is selected in the language menu and writes the configuration
	 * to the language configuration file which will be loaded at the start of the application
	 * @param locale the new language setting which should be stored
	 */
	private void setLanguage(Locale locale) {
		String confFilePath = new String("etc/language.setting"); 
		FileWriter fw;
		try {
			fw = new FileWriter(confFilePath);
			fw.write(locale.getLanguage());
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * A convenience method for adding a help menu to the main menu.
	 */
	private void addHelpMenu() {
		JMenu menu = new JMenu(m_languageBundle.getString("key_help"));
		JMenuItem tmp = new JMenuItem(m_languageBundle.getString("key_about"));
		tmp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				m_controller.openAbout();
			}
		});
		menu.add(tmp);
		m_menuBar.add(menu);
	}
	
	/**
	 * A convenience method for checking whether a model is registered.
	 * @return true or false
	 */
	private boolean isModelRegistered() {
		return m_model != null;
	}
	
	/**
	 * A convenience method for fetching new information from the model. 
	 * This is a really expensive action. Be aware of that!
	 */
	private void refreshAllTabs() {
		synchronized (m_refreshLock) {
			if (isModelRegistered()) {
				// disable the ChangeListener of m_tabbedPane
				m_refreshInProgress = true;
				
				// disable the user to modify m_tabbedPane while processing
				m_tabbedPane.setEnabled(false);
				
				// remove all tabs
				m_tabbedPane.removeAll();
				
				// load everything from the model (really expensive)
				for (int i = 0; i < m_model.getWorkspaceCount(); ++i) {
					TabObject tmp = m_model.getWorkspace(i);
					String name = "";
					
					// create a new tab with a certain content
					switch (tmp.getTabState()) {
					case WELCOME:
						m_tabbedPane.add(StaticDialogs.makeWelcomeTab());
						name = m_languageBundle.getString("key_welcome");
						break;
					case FILE_OPEN:
						m_tabbedPane.add(StaticDialogs.makeOpenFileTab());
						name = m_languageBundle.getString("key_open");
						break;
					case DIR_OPEN:
						m_tabbedPane.add(StaticDialogs.makeOpenDirTab());
						name = m_languageBundle.getString("key_open");
						break;
					case ERROR_OPEN:
						m_tabbedPane.add(StaticDialogs.makeErrorOpenTab(tmp.getName()));
						name = m_languageBundle.getString("key_error");
						break;
					case ABOUT:
						m_tabbedPane.add(StaticDialogs.makeAboutTab());
						name = m_languageBundle.getString("key_about");
						break;
					case PLUGIN_ACTIVE:
						m_tabbedPane.add(tmp.getContent());
						name = tmp.getName();
						break;
					}
					
					// add a title and a close button to the TabObject
					m_tabbedPane.setTabComponentAt(m_tabbedPane.getTabCount() - 1, new TabTitle(name));
					
					// select the tab if the model wants that to happen and get all supported plug-ins from the TabObject
					if (tmp.isTabActive()) {
						m_tabbedPane.setSelectedIndex(i);
						addPluginMenuEntries(tmp.getSuitablePlugins());
					}
				}
				
				// reactivate the ChangeListener of m_tabbedPane
				m_refreshInProgress = false;
				
				// enable the user to use m_tabbedPane
				m_tabbedPane.setEnabled(true);
			}
		}
	}
	
	/**
	 * This class holds all static dialogs and their convenience functions
	 * @author heidi
	 *
	 */
	private static class StaticDialogs {
		/**
		 * convenience method for building an welcome tab
		 * @return a JPanel
		 */
		protected static JComponent makeWelcomeTab() {
			JPanel content = new JPanel(new BorderLayout(5 , 5), false);
			JPanel contentHead = new JPanel(new BorderLayout(5, 0), false);
			content.add(contentHead, BorderLayout.NORTH);
			
			contentHead.add(makeMessage(m_languageBundle.getString("key_html_welcome")), BorderLayout.NORTH);
			contentHead.add(makeOpenButtons(), BorderLayout.SOUTH);
			
			return content;
		}
		
		/**
		 * convenience method for building an file open dialog tab
		 * @return a JPanel
		 */
		protected static JComponent makeOpenFileTab() {
			JPanel content = new JPanel(new BorderLayout(5 , 5), false);
			JPanel contentHead = new JPanel(new BorderLayout(5, 0), false);
			content.add(contentHead, BorderLayout.NORTH);
			
			contentHead.add(makeMessage(m_languageBundle.getString("key_html_openFile")), BorderLayout.NORTH);
			
			JPanel control = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0), false);
			JFileChooser filechooser = new JFileChooser(m_lastSelectedFilePath);
			filechooser.setDialogType(JFileChooser.OPEN_DIALOG);
			filechooser.setLocale(m_languageBundle.getLocale());		// Set the current language to the file chooser!
			filechooser.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JFileChooser chooser = (JFileChooser) e.getSource();
					if (JFileChooser.APPROVE_SELECTION.equals(e.getActionCommand())) {
						m_lastSelectedFilePath = chooser.getSelectedFile().getAbsolutePath();
						m_controller.openDicomFile(chooser.getSelectedFile().getPath());
					}
					else if (JFileChooser.CANCEL_SELECTION.equals(e.getActionCommand()))
						m_controller.closeWorkspace();
				}
			});
			control.add(filechooser);
			contentHead.add(control, BorderLayout.SOUTH);
			
			return content;
		}
		
		/**
		 * convenience method for building an file open dialog tab
		 * @return a JPanel
		 */
		protected static JComponent makeOpenDirTab() {
			JPanel content = new JPanel(new BorderLayout(5 , 5), false);
			JPanel contentHead = new JPanel(new BorderLayout(5, 0), false);
			content.add(contentHead, BorderLayout.NORTH);
			
			contentHead.add(makeMessage(m_languageBundle.getString("key_html_openDir")), BorderLayout.NORTH);
			
			JPanel control = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0), false);
			JFileChooser filechooser = new JFileChooser();
			filechooser.setDialogType(JFileChooser.OPEN_DIALOG);
			filechooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			filechooser.setLocale(m_languageBundle.getLocale());		// Set the current language to the file chooser!
			filechooser.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JFileChooser chooser = (JFileChooser) e.getSource();
					if (JFileChooser.APPROVE_SELECTION.equals(e.getActionCommand()))
						m_controller.openDicomDirectory(chooser.getSelectedFile().getPath());
					else if (JFileChooser.CANCEL_SELECTION.equals(e.getActionCommand()))
						m_controller.closeWorkspace();
				}
			});
			control.add(filechooser);
			contentHead.add(control, BorderLayout.SOUTH);
			
			return content;
		}
		
		/**
		 * convenience method for building an error open tab
		 * @return a JPanel
		 */
		protected static JComponent makeErrorOpenTab(String msg) {
			JPanel content = new JPanel(new BorderLayout(5 , 5), false);
			JPanel contentHead = new JPanel(new BorderLayout(5, 0), false);
			content.add(contentHead, BorderLayout.NORTH);
			
			contentHead.add(makeMessage(m_languageBundle.getString("key_html_errOpenFile")), BorderLayout.NORTH);
			contentHead.add(makeOpenButtons(), BorderLayout.CENTER);
			contentHead.add(makeMessage("Error code: " + msg), BorderLayout.SOUTH);
			
			return content;
		}
		
		/**
		 * convenience method for building an about tab
		 * @return a JPanel
		 */
		protected static JComponent makeAboutTab() {
			JPanel content = new JPanel(new BorderLayout(5 , 5), false);
			
			JPanel contentHead = new JPanel(new BorderLayout(5, 0), false);
			contentHead.add(makeMessage(m_languageBundle.getString("key_html_about")), BorderLayout.NORTH);
			content.add(contentHead, BorderLayout.CENTER);
			
			JPanel logos = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
			logos.add(new JLabel(new ImageIcon("etc/images/logo_big.png")));
			logos.add(new JLabel(new ImageIcon("etc/images/gplv3.png")));
			content.add(logos, BorderLayout.SOUTH);
			
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
			retVal.add(filler);
			return retVal;
		}
		
		/**
		 * convenience method for adding open buttons to a static dialog
		 * @return a JPanel with open buttons
		 */
		private static JComponent makeOpenButtons() {
			JPanel retVal = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0), false);
			JButton tmp = new JButton(m_languageBundle.getString("key_openFile"));
			tmp.setIcon(new ImageIcon("etc/images/text-x-generic.png"));
			tmp.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					m_controller.openDicomFileDialog();
				}
			});
			retVal.add(tmp);
			
			tmp = new JButton(m_languageBundle.getString("key_openDir"));
			tmp.setIcon(new ImageIcon("etc/images/folder.png"));
			tmp.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					m_controller.openDicomDirectoryDialog();
				}
			});
			retVal.add(tmp);
			
			tmp = new JButton(m_languageBundle.getString("key_exit"));
			tmp.setIcon(new ImageIcon("etc/images/system-log-out.png"));
			tmp.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					m_controller.closeApplication();
				}
			});
			retVal.add(tmp);
			
			return retVal;
		}
	}
	
	/**
	 * A convenience class for creating a JPanel with a title and a button, <br/>
	 * which triggers the close of the currently active workspace<br/>
	 * This might be used for the title of all tabs
	 * 
	 * This class was a part of the Java Tutorial
	 * http://java.sun.com/docs/books/tutorial/uiswing/examples/components/TabComponentsDemoProject/src/components/ButtonTabComponent.java
	 *
	 * Copyright (c) 1995 - 2008 Sun Microsystems, Inc.  All rights reserved.
	 *
	 * Redistribution and use in source and binary forms, with or without
	 * modification, are permitted provided that the following conditions
	 * are met:
	 *
	 *   - Redistributions of source code must retain the above copyright
	 *     notice, this list of conditions and the following disclaimer.
	 *
	 *   - Redistributions in binary form must reproduce the above copyright
	 *     notice, this list of conditions and the following disclaimer in the
	 *     documentation and/or other materials provided with the distribution.
	 *
	 *   - Neither the name of Sun Microsystems nor the names of its
	 *     contributors may be used to endorse or promote products derived
	 *     from this software without specific prior written permission.
	 *
	 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
	 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
	 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
	 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
	 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
	 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
	 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
	 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
	 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
	 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
	 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
	 */
	private class TabTitle extends JPanel {
		private static final long serialVersionUID = 682821987337403501L;

		public TabTitle(String name) {
			super(new FlowLayout(FlowLayout.LEFT, 0, 0));
			
			if (name == null) {
				throw new NullPointerException();
			}
			setOpaque(false);
			
			JLabel label = new JLabel(name);
			//add more space between the label and the button
			label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
			add(label);
			
			//tab button
			JButton button = new TabButton();
			add(button);
			
			//add more space to the top of the component
			setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
		}
		
		private class TabButton extends JButton implements ActionListener {
			private static final long serialVersionUID = 6661492050736259563L;
			private final int buttonSize = 17;
			
			public TabButton() {
				setPreferredSize(new Dimension(buttonSize, buttonSize));
				setToolTipText(m_languageBundle.getString("key_closeTab"));
				setUI(new BasicButtonUI());
				setContentAreaFilled(false);
				setFocusable(false);
				setBorder(BorderFactory.createEtchedBorder());
				setBorderPainted(false);
				setRolloverEnabled(true);
				addActionListener(this);
			}
			
			public void actionPerformed(ActionEvent e) {
				int i = m_tabbedPane.indexOfTabComponent(TabTitle.this);
				if (i != -1) {
					m_tabbedPane.setSelectedIndex(i);
					m_controller.closeWorkspace();
				}
			}
			
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g.create();
				
				if (getModel().isPressed()) {
					g2.translate(1, 1);
				}
				g2.setStroke(new BasicStroke(2));
				g2.setColor(Color.BLACK);
				if (getModel().isRollover()) {
					g2.setColor(Color.LIGHT_GRAY);
				}
				int delta = 6;
				g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
				g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
				g2.dispose();
			}
		}
	}
}
