package dicomux;

import java.util.Vector;

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
	 * clears the vector and adds a welcome message to index 0
	 * @return 
	 */
	public void initialize() {
		m_tabObjects.clear();
		m_tabObjects.add(new TabObject(TabState.WELCOME));
	}
	
	/**
	 * adds a new TabObject to the Model
	 * @param tab
	 */
	public void addTabObject(TabObject tab) {
		m_tabObjects.add(tab);
	}
	
	public TabObject getTabObject(int index) {
		return m_tabObjects.get(index);
	}
}
