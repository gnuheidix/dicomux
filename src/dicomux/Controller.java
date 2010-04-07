package dicomux;

/**
 * Controller for Dicomux / Serves as container for all necessary ActionListeners
 * @author heidi
 *
 */
public class Controller {
	/**
	 * holds the model of the application
	 */
	private Model m_model;
	
	/**
	 * holds the view of the application
	 */
	private View m_view;
	
	//TODO error handling (check for crap)
	/**
	 * default constructor
	 * @param model
	 * @param view
	 */
	public Controller(Model model, View view) {
		m_model = model;
		m_view = view;
		m_model.registerView(m_view);
		m_view.registerModel(m_model);
		initialize();
	}
	
	//TODO error handling (check wheather there is crap in m_*)
	/**
	 * initialization of all parts of the application
	 */
	private void initialize() {
		m_model.initialize();
		m_view.setVisible(true);
	}
	
}
