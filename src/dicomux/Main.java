package dicomux;

/**
 * Launches Dicomux by determining, which model, view and controller shall be used.
 * @author heidi
 *
 */
public class Main {
	public static void main(String[] args) {
		// create model and view
		IView view = new View();
		IModel model = new Model(view);
		
		// register the model on the view
		view.registerModel(model);
		
		// create a controller and register him on the view
		Controller ctrl;
		try {
			ctrl = new Controller(model, view);
			view.registerController(ctrl);
		} catch (Exception e) {
			System.err.println("Controller instantiation failed!");
			e.printStackTrace();
			System.exit(1);
		}
	}
}
