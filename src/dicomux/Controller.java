package dicomux;

import java.io.File;
import java.util.Locale;
import java.util.Vector;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.io.DicomInputStream;

/**
 * Controller for Dicomux / Serves as a container for all necessary methods which alter the model
 * @author heidi
 */
public class Controller implements IController {
	/**
	 * holds instances of all available plug-ins<br/>
	 * It's very important that plug-ins without any keyFormats are at the end of the list.
	 */
	private final Vector<APlugin> m_availblePlugins;
	
	/**
	 * holds the model of the application
	 */
	private IModel m_model;
	
	/**
	 * holds the view of the application
	 */
	private IView m_view;
	
	/**
	 * default constructor<br/>
	 * registers the view in the model and vice versa<br>
	 * calls initialize() of the model
	 * @param model
	 * @param view
	 * @throws Exception 
	 * @see IModel
	 * @see IView
	 */
	public Controller(IModel model, IView view) {
		m_model = model;
		m_view = view;
		
		m_availblePlugins = new Vector<APlugin>();
		try {
			m_availblePlugins.add(new WaveformPlugin());
			m_availblePlugins.add(new PDFPlugin());
			m_availblePlugins.add(new PatientDataPlugin());
		} catch (Exception e) {
			System.err.println("Failure during plug-in instatiation! Some plug-ins may be not availble.");
		}
		
		m_availblePlugins.add(new RawPlugin());
	}
	
	@Override
	public void closeAllWorkspaces() {
		m_model.initialize();
	}
	
	@Override
	public void closeWorkspace() {
		m_model.removeWorkspace(m_view.getActiveWorkspaceId());
	}
	
	@Override
	public void openAbout() {
		for (int i = 0; i < m_model.getWorkspaceCount(); ++i) {
			switch (m_model.getWorkspace(i).getTabState()) {
			case ABOUT: m_model.setWorkspace(i, new TabObject(TabState.ABOUT, true)); return;
			}
		}
		m_model.addWorkspace(new TabObject(TabState.ABOUT));
	}
	
	@Override
	public void openDicomDirectoryDialog() {
		for (int i = 0; i < m_model.getWorkspaceCount(); ++i) {
			switch (m_model.getWorkspace(i).getTabState()) {
			case ERROR_OPEN:
			case FILE_OPEN:
			case DIR_OPEN:
			case WELCOME: m_model.setWorkspace(i, new TabObject(TabState.DIR_OPEN, true)); return;
			}
		}
		m_model.addWorkspace(new TabObject(TabState.DIR_OPEN));
	}
	
	@Override
	public void openDicomFileDialog() {
		for (int i = 0; i < m_model.getWorkspaceCount(); ++i) {
			switch (m_model.getWorkspace(i).getTabState()) {
			case ERROR_OPEN:
			case FILE_OPEN:
			case DIR_OPEN:
			case WELCOME: m_model.setWorkspace(i, new TabObject(TabState.FILE_OPEN, true)); return;
			}
		}
		m_model.addWorkspace(new TabObject(TabState.FILE_OPEN));
	}
	
	@Override
	public void closeApplication() {
		System.exit(0);
	}
	
	@Override
	public void openDicomFile(String path) {
		try {
			// open the dicom file
			File fileObject = new File(path);
			DicomInputStream din = new DicomInputStream(fileObject);
			DicomObject dicomObject = din.readDicomObject();
			din.close();
			
			// look for a suitable plug-in for the opened DicomObject
			APlugin chosenPlugin = null;
			Vector<APlugin> suitablePlugins = new Vector<APlugin>();
			for (int i = 0; i < m_availblePlugins.size(); ++i) { // iterate over all available plug-ins
				APlugin tmp = m_availblePlugins.get(i);
				// does the selected plug-in support our DicomObject? 
				if (tmp.getKeyTag().checkDicomObject(dicomObject)) {
					suitablePlugins.add(tmp);
				}
			}
			
			if (suitablePlugins.size() > 0) {
				// select the first plug-in of the suitable plug-ins
				chosenPlugin = suitablePlugins.firstElement().getClass().newInstance();
				
				// push the currently used language to the new plug-in
				chosenPlugin.setLanguage(m_view.getLanguage());
				
				// push the DicomObject to the plug-in
				chosenPlugin.setData(dicomObject);
				
				// create a new TabObject and fill it with all we got
				TabObject tmp = new TabObject();
				tmp.setDicomObj(dicomObject);
				tmp.setTabActive(true);
				tmp.setName(fileObject.getName());
				tmp.setTabState(TabState.PLUGIN_ACTIVE);
				tmp.setPlugin(chosenPlugin);
				tmp.setSuitablePlugins(suitablePlugins);
				
				// push the new TabObject to our workspace
				m_model.setWorkspace(m_view.getActiveWorkspaceId(), tmp);
			}
			else
				throw new Exception("No suitable plug-in found!");
		}
		catch (Exception e) {
			// something didn't work - let's show an error message
			TabObject errorTab = new TabObject(TabState.ERROR_OPEN, true);
			errorTab.setName(e.getMessage());
			m_model.setWorkspace(m_view.getActiveWorkspaceId(), errorTab);
			e.printStackTrace();
			return;
		}
	}
	
	@Override
	public void setActiveWorkspace(int n) {
		m_model.setActiveWorkspace(n);
	}
	
	@Override
	public void openDicomDirectory(String path) {
		try{
			// open the dicom file
			File fileObject = new File(path);
			DicomInputStream din = new DicomInputStream(fileObject);
			DicomObject dicomObject = din.readDicomObject();
			din.close();
			
			// TODO: Do we have more Plugins in DirectoryMode ??
			DirectoryPlugin chosenPlugin = new DirectoryPlugin();
			// push the currently used language to the new plug-in
			chosenPlugin.setLanguage(m_view.getLanguage());
			
			chosenPlugin.setDirFilePath(fileObject.getParent());
			// push the DicomObject to the plug-in
			chosenPlugin.setData(dicomObject);
			// create a new TabObject and fill it with all we got
			TabObject tmp = new TabObject();
			tmp.setDicomObj(dicomObject);
			tmp.setTabActive(true);
			tmp.setName(fileObject.getName());
			tmp.setTabState(TabState.PLUGIN_ACTIVE);
			tmp.setPlugin(chosenPlugin);
			m_model.setWorkspace(m_view.getActiveWorkspaceId(), tmp);
			return;
		}
		catch (Exception e) {
			// something didn't work - let's show an error message
			TabObject errorTab = new TabObject(TabState.ERROR_OPEN, true);
			errorTab.setName(e.getMessage());
			m_model.setWorkspace(m_view.getActiveWorkspaceId(), errorTab);
			e.printStackTrace();
			return;
		}
	}
	
	@Override
	public void setLanguage(Locale locale) {
		if (locale != null) {
			m_view.setLanguage(locale);
			for (int i = 0; i < m_model.getWorkspaceCount(); ++i) {
				TabObject selectedWorkspace = m_model.getWorkspace(i);
				APlugin selectedPlugin = selectedWorkspace.getPlugin();
				if (selectedPlugin != null)
					selectedPlugin.setLanguage(locale);
			}
		}
	}
	
	@Override
	public void setActivePlugin(String name) {
		try {
			// search for the plug-in with the suitable name
			for (int i = 0; i < m_availblePlugins.size(); ++i) {
				if (m_availblePlugins.get(i).getName().equals(name)) {
					// get the active workspace ID from the view
					int activeWorkspaceId = m_view.getActiveWorkspaceId();
					
					// extract the TabObject from the model
					TabObject tmp = m_model.getWorkspace(activeWorkspaceId);
					
					// create a new instance of the selected plug-in
					APlugin selectedPlugin = m_availblePlugins.get(i).getClass().newInstance();
					
					// initialize the selected plug-in with all needed data
					selectedPlugin.setLanguage(m_view.getLanguage());
					selectedPlugin.setData(tmp.getDicomObj());
					
					// bind the plug-in to the workspace and write all changes to the model
					tmp.setPlugin(selectedPlugin);
					m_model.setWorkspace(activeWorkspaceId, tmp);
				}
			}
		} catch (Exception e) {
			// something didn't work - let's show an error message
			m_model.setWorkspace(m_view.getActiveWorkspaceId(), new TabObject(TabState.ERROR_OPEN, true));
			e.printStackTrace();
			return;
		}
	}
}
