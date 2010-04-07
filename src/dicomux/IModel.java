package dicomux;

public interface IModel {
	public void registerView(IView view);
	public TabObject getWorkspace(int i);
	public int getWorkspaceCount();
	public void initialize();
}
