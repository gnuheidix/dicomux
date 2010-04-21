package dicomux;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.dcm4che2.data.DicomObject;

/**
 * data transfer object between IModel and IView
 * @author heidi
 * @see TabState
 */
public class TabObject {
	/**
	 * Dicom file of the workspace
	 */
	private DicomObject m_dicomObj;
	
	/**
	 * Plug-in which is currently bound to that workspace
	 */
	private IPlugin m_plugin;
	
	/**
	 * Name of that workspace
	 */
	private String m_name;
	
	/**
	 * State of that workspace
	 */
	private TabState m_tabState;
	
	/**
	 * States whether this tab is currently selected
	 */
	private boolean m_tabActive;
	
	/**
	 * default constructor
	 */
	public TabObject() {
		m_dicomObj = null;
		m_plugin = null;
		m_name = "";
		m_tabState = null;
		m_tabActive = false;
	}
	
	/**
	 * @param tabState
	 */
	public TabObject(TabState tabState) {
		m_dicomObj = null;
		m_plugin = null;
		m_name = null;
		m_tabState = tabState;
		m_tabActive = false;
	}
	
	/**
	 * @param tabState
	 * @param tabActive
	 */
	public TabObject(TabState tabState, boolean tabActive) {
		m_dicomObj = null;
		m_plugin = null;
		m_name = null;
		m_tabState = tabState;
		m_tabActive = tabActive;
	}
	
	/**
	 * 
	 * @return content from the plugin
	 */
	public JComponent getContent() {
		if (m_plugin != null)
			return m_plugin.getContent();
		else
			return new JLabel("ERROR: No plug-in loaded for this tab!");
	}
	
	/**
	 * @return the m_dicomObj
	 */
	public DicomObject getDicomObj() {
		return m_dicomObj;
	}
	
	/**
	 * @param dicomObj the m_dicomObj to set
	 */
	public void setDicomObj(DicomObject dicomObj) {
		m_dicomObj = dicomObj;
	}
	
	/**
	 * @return the m_plugin
	 */
	public IPlugin getPlugin() {
		return m_plugin;
	}
	
	/**
	 * @param plugin the m_plugin to set
	 */
	public void setPlugin(IPlugin plugin) {
		m_plugin = plugin;
	}
	
	/**
	 * @return the m_name
	 */
	public String getName() {
		return m_name;
	}
	
	/**
	 * @param name the m_name to set
	 */
	public void setName(String name) {
		m_name = name;
	}
	
	/**
	 * @return the m_tabState
	 */
	public TabState getTabState() {
		return m_tabState;
	}
	
	/**
	 * @param tabState
	 */
	public void setTabContent(TabState tabState) {
		m_tabState = tabState;
	}
	
	/**
	 * @param m_tabActive
	 */
	public void setTabActive(boolean m_tabActive) {
		this.m_tabActive = m_tabActive;
	}
	
	/**
	 * @return the m_tabActive
	 */
	public boolean isTabActive() {
		return m_tabActive;
	}
}