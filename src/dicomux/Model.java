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
	
	@Override
	public void initialize() {
		m_tabObjects.clear();
		addWorkspace(new TabObject(TabState.WELCOME));
	}
	
	@Override
	public void setWorkspace(int wsId, TabObject tab) {
		m_tabObjects.setElementAt(tab, wsId);
		if (tab.isTabActive())
			setActiveWorkspace(wsId);
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
		if (wsId < m_tabObjects.size() && wsId >= 0) {
			m_tabObjects.remove(wsId);
			
			if (m_tabObjects.size() == 0)
				initialize();
			else {
				setActiveWorkspace(m_tabObjects.size() - 1);
				m_view.notifyView();
			}
		}
	}
	
	@Override
	public void addWorkspace(TabObject tab) {
		m_tabObjects.add(tab);
		setActiveWorkspace(m_tabObjects.size() - 1);
		m_view.notifyView();
	}
	
	@Override
	public void setActiveWorkspace(int wsId) {
		if (wsId >= 0 && wsId < m_tabObjects.size()) {
			for(TabObject i: m_tabObjects) { // set all workspaces to inactive
				i.setTabActive(false);
			}
			m_tabObjects.get(wsId).setTabActive(true); // set the specified workspace to active
		}
	}
}
