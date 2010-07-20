package dicomux;

import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.dcm4che2.data.DicomObject;

/**
 * data transfer object between IModel and IView
 * @author heidi
 * @see TabState
 * @see APlugin
 */
public class TabObject {
	/**
	 * Dicom file of the workspace
	 */
	private DicomObject m_dicomObj;
	
	/**
	 * Plug-in which is currently bound to that workspace
	 */
	private APlugin m_activePlugin;
	
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
	 * holds all suitable plug-ins for the DicomObject
	 */
	private Vector<APlugin> m_suitablePlugins;
	
	/**
	 * default constructor
	 */
	public TabObject() {
		m_dicomObj = null;
		m_activePlugin = new PluginAdapter();
		m_name = "";
		m_tabState = TabState.WELCOME;
		m_tabActive = false;
		m_suitablePlugins = new Vector<APlugin>();
	}
	
	/**
	 * @param tabState
	 */
	public TabObject(TabState tabState) {
		m_dicomObj = null;
		m_activePlugin = new PluginAdapter();
		m_name = "";
		m_tabState = tabState;
		m_tabActive = false;
		m_suitablePlugins = new Vector<APlugin>();
	}
	
	/**
	 * @param tabState
	 * @param tabActive
	 */
	public TabObject(TabState tabState, boolean tabActive) {
		m_dicomObj = null;
		m_activePlugin = new PluginAdapter();
		m_name = "";
		m_tabState = tabState;
		m_tabActive = tabActive;
		m_suitablePlugins = new Vector<APlugin>();
	}
	
	/**
	 * 
	 * @return content from the plugin
	 */
	public JComponent getContent() {
		if (m_activePlugin != null)
			return m_activePlugin.getContent();
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
	public APlugin getPlugin() {
		return m_activePlugin;
	}
	
	/**
	 * @param plugin the m_plugin to set
	 */
	public void setPlugin(APlugin plugin) {
		m_activePlugin = plugin;
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
	public void setTabState(TabState tabState) {
		m_tabState = tabState;
	}
	
	/**
	 * @param tabActive
	 */
	public void setTabActive(boolean tabActive) {
		this.m_tabActive = tabActive;
	}
	
	/**
	 * @return the m_tabActive
	 */
	public boolean isTabActive() {
		return m_tabActive;
	}
	
	/**
	 * 
	 * @param suitablePlugins
	 */
	public void setSuitablePlugins(Vector<APlugin> suitablePlugins) {
		m_suitablePlugins = suitablePlugins;
	}
	
	/**
	 * 
	 * @return all suitable plug-ins for the opened DicomObject
	 */
	public Vector<APlugin> getSuitablePlugins() {
		return m_suitablePlugins;
	}
}