package dicomux;

/**
 * an interface for all models of Dicomux
 * @author heidi
 *
 */
public interface IModel {
	/**
	 * registers a view which performs get-calls only
	 * @param view the view which should be registered
	 */
	public void registerView(IView view);
	
	/**
	 *
	 * @param n the index of the workspace
	 * @return the workspace with a certain index
	 */
	public TabObject getWorkspace(int n);
	
	/**
	 * @return the number of existing workspaces
	 */
	public int getWorkspaceCount();
	
	/**
	 * sets a TabObject in the model
	 * @param tab
	 */
	public void setWorkspace(int wsId, TabObject tab);
	
	/**
	 * removes a workspace from the model
	 * @param wsId
	 */
	public void removeWorkspace(int wsId);
	/**
	 * initalizes the whole model and notifies the view
	 * @return 
	 */
	public void initialize();
}
