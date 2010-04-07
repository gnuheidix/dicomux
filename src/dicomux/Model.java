package dicomux;

import java.util.Vector;

/**
 * Model for Dicomux
 * @author heidi
 *
 */
public class Model implements IModel {
	/**
	 * the TabObject's vector index is equal to the tabIndex
	 */
	private Vector<TabObject> m_tabObjects;
	
	/**
	 * the view which observes the model
	 */
	private IView m_view;
	
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
		m_view.notifyView();
	}
	
	/**
	 * adds a new TabObject to the Model
	 * @param tab
	 */
	public void addWorkspace(TabObject tab) {
		m_tabObjects.add(tab);
		m_view.notifyView();
	}
	
	@Override
	public void registerView(IView view) {
		m_view = view;
	}

	@Override
	public TabObject getWorkspace(int i) {
		return m_tabObjects.get(i);
	}

	@Override
	public int getWorkspaceCount() {
		return m_tabObjects.size();
	}
}
