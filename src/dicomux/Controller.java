package dicomux;

/**
 * Controller for Dicomux / Serves as container for all necessary ActionListeners
 * @author heidi
 *
 */
public class Controller {
	private Model m_model;
	private View m_view;
	
	public Controller(Model model, View view) {
		m_model = model;
		m_view = view;
		
		initialize();
	}

	private void initialize() {
		m_model.initialize();
		m_view.addTab(new TabObject(TabState.WELCOME));
		m_view.setVisible(true);
	}
	
}
