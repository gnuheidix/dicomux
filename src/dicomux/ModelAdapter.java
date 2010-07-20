package dicomux;

/**
 * This is an adapter for IModel. This is used during initialization of Dicomux
 * @author heidi
 *
 */
public class ModelAdapter implements IModel {

	@Override
	public void addWorkspace(TabObject tab) {
		// TODO Auto-generated method stub

	}

	@Override
	public TabObject getWorkspace(int n) {
		return new TabObject();
	}

	@Override
	public int getWorkspaceCount() {
		return 1;
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeWorkspace(int wsId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setActiveWorkspace(int wsId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setWorkspace(int wsId, TabObject tab) {
		// TODO Auto-generated method stub

	}

}
