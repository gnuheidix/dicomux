package dicomux;

import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.dcm4che2.data.DicomObject;

/**
 * Model for Dicomux
 * @author heidi
 *
 */
public class Model {
	/**
	 * the TabObject's vector index is equal to the tabIndex
	 */
	private Vector<TabObject> m_tabObjects;
	
	/**
	 * default constructor
	 */
	public Model() {
		m_tabObjects = new Vector<TabObject>(5, 5);
	}
	
	/**
	 * clears the vector and adds a welcome message
	 * @return 
	 */
	public void initialize() {
		m_tabObjects.clear();
		m_tabObjects.add(new TabObject(TabState.WELCOME));
	}
	
	/**
	 * data transfer object for tabs
	 * @author heidi
	 *
	 */
	public class TabObject {
		private dicomux.TabObject data = new dicomux.TabObject();

		/**
		 * default constructor
		 */
		public TabObject() {
			data.m_dicomObj = null;
			data.m_plugin = null;
			data.m_name = "";
			data.m_tabState = null;
		}
		
		/**
		 * @param dicomObj
		 * @param plugin
		 * @param name
		 * @param tabContent
		 */
		public TabObject(TabState tabState) {
			data.m_dicomObj = null;
			data.m_plugin = null;
			data.m_name = null;
			data.m_tabState = tabState;
		}
		
		public JComponent getContent() {
			if (data.m_plugin != null)
				return data.m_plugin.getContent();
			else
				return new JLabel("ERROR: No plug-in loaded for this tab!");
		}
		
		/**
		 * @return the m_dicomObj
		 */
		public DicomObject getDicomObj() {
			return data.m_dicomObj;
		}
		
		/**
		 * @param mDicomObj the m_dicomObj to set
		 */
		public void setDicomObj(DicomObject dicomObj) {
			data.m_dicomObj = dicomObj;
		}
		
		/**
		 * @return the m_plugin
		 */
		public Plugin getPlugin() {
			return data.m_plugin;
		}
		
		/**
		 * @param mPlugin the m_plugin to set
		 */
		public void setPlugin(Plugin plugin) {
			data.m_plugin = plugin;
		}
		
		/**
		 * @return the m_name
		 */
		public String getName() {
			return data.m_name;
		}
		
		/**
		 * @param mName the m_name to set
		 */
		public void setName(String name) {
			data.m_name = name;
		}
		
		/**
		 * @return the m_tabState
		 */
		public TabState getTabState() {
			return data.m_tabState;
		}
		
		/**
		 * @param mTabContent the m_tabContent to set
		 */
		public void setTabContent(TabState tabState) {
			data.m_tabState = tabState;
		}
	}
}
