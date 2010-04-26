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
	 * contains the tabbed pane which holds all workspaces
	 */
	private static JTabbedPane m_tabbedPane;
	
	/**
	 * contains the menu bar of the application
	 */
	private JMenuBar m_menuBar;
	
	/**
	 * the model which serves as data source
	 */
	private IModel m_model = null;
	
	/**
	 * this object (for access from an inner class)
	 */
	private static View m_view;
	
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
		m_view = this;
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
		setIconImage(new ImageIcon("etc/logo.gif").getImage());
		
		// extract own contentPane and set its layout manager
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout(0, 5));
		
		// create a main menu and add it to the View
		m_menuBar = new JMenuBar();
		setJMenuBar(m_menuBar);
		
		// add menu entries to the main menu
		initializeMenus();
		
		// create a tabbed pane, set a ChangeListener and add it to the content pane
		m_tabbedPane = new JTabbedPane();
		m_tabbedPane.setTabPlacement(JTabbedPane.TOP);
		m_tabbedPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				synchronized (m_refreshLock) {
					if (!m_refreshInProgress) {
						System.out.println(e.getSource().toString());
						m_controller.setActiveWorkspace(m_tabbedPane.getSelectedIndex());
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
	
	//TODO extend this
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
		// ... add more of these calls in order to localize the whole JFileChooser
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
				m_controller.reinitializeApplicationDialog();
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
	
	//TODO needs to be extended
	/**
	 * A convenience method for fetching new information from the model. 
	 * This is a really expensive action. Be aware of that!
	 */
	private void refreshAllTabs() {
		synchronized (m_refreshLock) {
			if (isModelRegistered()) {
				// disable the user to modify m_tabbedPane while processing
				m_tabbedPane.setEnabled(false);
				
				// disable the ChangeListener of m_tabbedPane
				m_refreshInProgress = true;
				
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
						m_tabbedPane.add(StaticDialogs.makeErrorOpenTab());
						name = m_languageBundle.getString("key_error");
						break;
					case ABOUT:
						m_tabbedPane.add(StaticDialogs.makeAboutTab());
						name = m_languageBundle.getString("key_about");
						break;
					case RESTART:
						m_tabbedPane.add(StaticDialogs.makeRestartTab(this));
						name = m_languageBundle.getString("key_restart");
						break;
					}
					
					// add a title and a close button to the tab
					m_tabbedPane.setTabComponentAt(m_tabbedPane.getTabCount() - 1, new TabTitle(name));
					
					// select the tab if the model wants that to happen
					if (tmp.isTabActive())
						m_tabbedPane.setSelectedIndex(i);
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
			// TODO: DateiName and DateiTyp not in actual Language ??
			filechooser.setLocale(m_languageBundle.getLocale());		// Set the actual language to the file chooser!
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
		protected static JComponent makeErrorOpenTab() {
			JPanel content = new JPanel(new BorderLayout(5 , 5), false);
			JPanel contentHead = new JPanel(new BorderLayout(5, 0), false);
			content.add(contentHead, BorderLayout.NORTH);
			
			contentHead.add(makeMessage(m_languageBundle.getString("key_html_errOpenFile")), BorderLayout.NORTH);
			contentHead.add(makeOpenButtons(), BorderLayout.SOUTH);
			
			return content;
		}
		
		/**
		 * convenience method for building an about tab
		 * @return a JPanel
		 */
		protected static JComponent makeAboutTab() {
			JPanel content = new JPanel(new BorderLayout(5 , 5), false);
			JPanel contentHead = new JPanel(new BorderLayout(5, 0), false);
			content.add(contentHead, BorderLayout.NORTH);
			
			contentHead.add(makeMessage(m_languageBundle.getString("key_html_about")), BorderLayout.NORTH);
			
			return content;
		}
		
		/**
		 * convenience method for building an about tab
		 * @return a JPanel
		 */
		protected static JComponent makeRestartTab(View dicomux) {
			JPanel content = new JPanel(new BorderLayout(5 , 5), false);
			JPanel contentHead = new JPanel(new BorderLayout(5, 0), false);
			content.add(contentHead, BorderLayout.NORTH);
			
			contentHead.add(makeMessage(m_languageBundle.getString("key_html_restart")), BorderLayout.NORTH);
			contentHead.add(makeRestartButtons(), BorderLayout.SOUTH);
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
		private static JComponent makeRestartButtons() {
			JPanel retVal = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0), false);
			JButton tmp = new JButton(m_languageBundle.getString("key_continueWork"));
			tmp.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					m_controller.closeWorkspace();
				}
			});
			retVal.add(tmp);
			
			tmp = new JButton(m_languageBundle.getString("key_restart"));
			tmp.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					m_controller.closeAllWorkspaces();
					m_view.initializeLanguage();
					m_view.initializeMenus();
					m_view.refreshAllTabs();
					m_view.repaint();
				}
			});
			retVal.add(tmp);
			
			return retVal;
		}
		
		/**
		 * convenience method for adding open buttons to a static dialog
		 * @return a JPanel with open buttons
		 */
		private static JComponent makeOpenButtons() {
			JPanel retVal = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0), false);
			JButton tmp = new JButton(m_languageBundle.getString("key_openFile"));
			tmp.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					m_controller.openDicomFileDialog();
				}
			});
			retVal.add(tmp);
			
			tmp = new JButton(m_languageBundle.getString("key_openDir"));
			tmp.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					m_controller.openDicomDirectoryDialog();
				}
			});
			retVal.add(tmp);
			
			tmp = new JButton(m_languageBundle.getString("key_exit"));
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