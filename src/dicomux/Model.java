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
	 * clears the vector, adds a welcome message and notifies the view
	 * @return 
	 */
	public void initialize() {
		m_tabObjects.clear();
		m_tabObjects.add(new TabObject(TabState.WELCOME));
		m_view.notifyView();
	}
	
	@Override
	public void setWorkspace(int wsId, TabObject tab) {
		m_tabObjects.setElementAt(tab, wsId);
		m_view.notifyView();
	}
	
	@Override
	public void registerView(IView view) {
		m_view = view;
	}
	
	@Override
	public TabObject getWorkspace(int n) {
		return m_tabObjects.get(n);
	}
	
	@Override
	public int getWorkspaceCount() {
		return m_tabObjects.size();
	}

	@Override
	public void removeWorkspace(int wsId) {
		if (m_tabObjects.size() > wsId) {
			m_tabObjects.remove(wsId);
			
			if (m_tabObjects.size() == 0)
				m_tabObjects.add(new TabObject(TabState.WELCOME));
			
			m_view.notifyView();
		}
	}
}
