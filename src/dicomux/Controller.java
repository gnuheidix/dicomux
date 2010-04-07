package dicomux;

/**
 * Controller for Dicomux / Serves as container for all necessary ActionListeners
 * @author heidi
 *
 */
public class Controller implements IController {
	/**
	 * holds the model of the application
	 */
	private IModel m_model;
	
	/**
	 * holds the view of the application
	 */
	private IView m_view;
	//TODO error handling (check for crap)
	/**
	 * default constructor<br/>
	 * registers the view in the model and vice versa<br>
	 * calls initialize() of the model
	 * @param model
	 * @param view
	 * @see IModel
	 * @see IView
	 */
	public Controller(IModel model, IView view) {
		m_model = model;
		m_view = view;
		
		m_model.registerView(m_view);
		m_view.registerModel(m_model);
		m_view.registerController(this);
		m_model.initialize();
	}
	
	@Override
	public void closeAllWorkspaces() {
		m_model.initialize();
	}
	
	@Override
	public void closeWorkspace() {
		m_model.removeWorkspace(m_view.getActiveWorkspaceId());
	}
	
	@Override
	public void openAboutInformation() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void openDicomDirectoryDialog() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void openDicomFileDialog() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void closeApplication() {
		System.exit(0);
	}
}
